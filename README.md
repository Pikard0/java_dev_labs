Звіт для лабараторної роботи 6
Метою лабораторної роботи є інтеграція Quartz Scheduler у застосунок Toy Store для запуску регулярних доменних задач за розкладом, який зберігається у базі даних. Рішення має бути стійким до перезапуску застосунку, не створювати дублікати робіт, мати визначену поведінку у разі пропущеного запуску та не допускати паралельної обробки однакових даних.
У межах роботи виконано налаштування Quartz JDBC Job Store для MariaDB, підключено керування схемою через Liquibase, створено два класи Quartz Job, три triggers, передачу параметрів через JobDataMap та тестове покриття всіх сценаріїв, визначених у завданні.
Вихідні дані та обґрунтування регулярних задач
Toy Store уже має сутності замовлення та іграшки. Для планування було обрано дві задачі, які відповідають доменній логіці магазину та можуть виконуватися без участі користувача.
Очищення застарілих замовлень: Замовлення зі статусом PLACED не повинні залишатися активними необмежений час. Такі замовлення переводяться у DECLINED після заданого часу очікування.
Звіт про малий залишок: Менеджеру потрібно регулярно бачити іграшки, запас яких наближається до вичерпання. У лог записується кількість позицій у звіті за заданим порогом залишку.
Завдання очищення змінює лише записи зі статусом PLACED. Після зміни статусу повторний запуск не знаходить цей самий запис, тому операція є ідемпотентною. Звітова задача працює у режимі лише читання і не модифікує дані.
Архітектура рішення
Конфігурація Quartz зосереджена в одному класі QuartzConfiguration. У ньому створюються JobDetail і Trigger beans з фіксованими назвою та групою. Стабільні ключі важливі для JDBC Job Store: після перезапуску Quartz відновлює існуючий опис із таблиць, а не створює нові дублікати.
Компоненти:
QuartzConfiguration (Опис jobs і triggers): Визначає JobKey, TriggerKey, JobDataMap, misfire та timezone.
AutowiringSpringBeanJobFactory (Інтеграція Quartz зі Spring): Створює екземпляр job і виконує autowiring сервісів Spring.
StaleOrderCleanupJob (Quartz Job): Зчитує параметри і викликає один метод OrderMaintenanceService.
LowStockReportJob (Quartz Job): Зчитує параметри і викликає один метод InventoryReportService.
OrderMaintenanceService (Доменна логіка): У транзакції переводить застарілі PLACED замовлення у DECLINED.
InventoryReportService (Доменна логіка): У read-only транзакції формує дані про малий залишок.
Jobs навмисно залишені мінімалістичними: вони не містять JPA-логіки, не працюють напряму з репозиторіями й не відкривають транзакції. Їхня роль - валідувати параметри JobDataMap і делегувати роботу одному сервісу Spring.
Налаштування Quartz JDBC Job Store і Liquibase
До Maven-проєкту додано залежність spring-boot-starter-quartz. У application.yml обрано тип сховища jdbc та вимкнено автоматичне створення схеми Quartz засобами Spring Boot. Таким чином, єдине джерело керування структурою бази даних - Liquibase.
spring:
quartz:
job-store-type: jdbc
overwrite-existing-jobs: false
jdbc:
initialize-schema: never
До master changelog додано changeset 007. Він запускає SQL-схему Quartz 2.5.2 для MariaDB InnoDB. Схема створює таблиці QRTZ_JOB_DETAILS, QRTZ_TRIGGERS, QRTZ_SIMPLE_TRIGGERS, QRTZ_CRON_TRIGGERS, QRTZ_FIRED_TRIGGERS, QRTZ_LOCKS та інші службові таблиці й індекси. Саме в них зберігаються jobs, triggers, дані запусків і блокування.
Встановлено scheduler name ToyStoreScheduler та JDBC delegate StdJDBCDelegate. Параметр overwrite-existing-jobs: false не дозволяє застосунку перезаписувати вже збережені визначення без явного рішення, що зменшує ризик дублювання або непомітної зміни розкладу після деплою.
Реалізовані jobs і triggers
Job stale-order-cleanup (staleHours=24, batchSize=100) -> Trigger stale-order-cleanup-hourly: Щогодини; NEXT_WITH_EXISTING_COUNT; batchSize=25.
Job stale-order-cleanup (staleHours=24, batchSize=100) -> Trigger stale-order-cleanup-nightly: Щодня о 02:00 Europe/Kyiv; DO_NOTHING; staleHours=48.
Job low-stock-report (minimumStock=5, reportName=low-stock-toys) -> Trigger low-stock-report-daily: Щодня о 18:00 Europe/Kyiv; DO_NOTHING; minimumStock=3.
Перший Job має два triggers, як вимагає завдання. Це демонструє, що один тип доменної операції може запускатися за різними правилами. Для нічного запуску визначено більший поріг давності замовлення, а для погодинного - менший розмір пакета.
Quartz об'єднує JobDataMap JobDetail і JobDataMap Trigger. Значення тригера мають пріоритет. Наприклад, погодинний trigger замінює batchSize=100 на batchSize=25, але зберігає staleHours=24 із JobDetail.
JobDetail: staleHours=24, batchSize=100
Trigger: batchSize=25
Merged map: staleHours=24, batchSize=25
Для cron-triggers явно встановлено TimeZone Europe/Kyiv. Це виключає залежність від локального часового поясу сервера. Для кожного trigger також задана інструкція misfire: cron-завдання не наздоганяють пропущений запуск, а очікують наступного планового часу; погодинне simple-завдання продовжує свій графік без лавиноподібного виконання пропущених задач.
Надійність, ідемпотентність і обробка помилок
Обидва класи jobs позначено @DisallowConcurrentExecution. Якщо новий момент запуску настає, коли попередній запуск того самого job ще триває, Quartz не запускає другий екземпляр паралельно. Це важливо для очищення замовлень, щоб один запис не оброблявся двічі одночасно.
Для OrderMaintenanceService транзакційна межа розташована на сервісному методі declineStalePlacedOrders. Метод вибирає лише замовлення PLACED, старші за заданий поріг, обмежує їх кількістю batchSize, змінює статус на DECLINED і зберігає результат. Після цього повторний запуск не знаходить ті самі записи. Отже, другий запуск не здійснює додаткової зміни даних.
Якщо сервіс кидає RuntimeException, job фіксує помилку у лог і передає Quartz JobExecutionException із refireImmediately=false. Обрана стратегія - не створювати нескінченний цикл повторних спроб. Quartz завершує поточний запуск і переходить до наступного запланованого trigger-а.
Відсутні або некоректні параметри JobDataMap також обробляються явно. Допоміжний клас QuartzJobData перевіряє наявність ключів, тип і додатність числових значень. Якщо ключ відсутній або має неправильний формат, job завершується зрозумілим JobExecutionException.
Тестування та результати перевірки
Для лабораторної додано модульні тести конфігурації, Quartz jobs, сервісу обслуговування замовлень і конфігурації персистентності. Перевірка виконувалася командами mvn test та mvn verify. Обидві команди завершилися успішно; verify додатково перевірив встановлений поріг покриття JaCoCo.
Результати перевірки сценаріїв із завдання:
Jobs і triggers в одному місці (QuartzConfigurationTest): JobKey і TriggerKey мають визначені назву та групу; trigger посилається на свій JobKey.
Пріоритет параметрів trigger (QuartzConfigurationTest): batchSize з trigger замінює default, staleHours з JobDetail зберігається.
Легкі jobs (QuartzJobsTest): Кожен job зчитує параметри та викликає один сервіс.
Відсутній параметр (QuartzJobsTest): Викидається JobExecutionException із назвою відсутнього ключа.
Заборона паралельності (QuartzJobsTest): Перевіряється анотація @DisallowConcurrentExecution на обох jobs.
Misfire і timezone (QuartzConfigurationTest): Перевіряються інструкції та Europe/Kyiv у cron-triggers.
Ідемпотентність (OrderMaintenanceServiceTest): Другий запуск для тих самих даних повертає 0 і не змінює запис повторно.
Збій job (QuartzJobsTest): JobExecutionException має refireImmediately=false.
JDBC schema через Liquibase (QuartzPersistenceConfigurationTest): Перевіряється SQL QRTZ_* і initialize-schema: never.

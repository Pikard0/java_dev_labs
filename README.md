Звіт до лабораторної роботи 7

У цій лабораторній роботі проєкт магазину іграшок було переведено з одного Maven-модуля у multi-module Maven-проєкт. Мета зміни полягала в тому, щоб мати один репозиторій і один загальний процес збірки, але запускати та тестувати окремі сервіси незалежно один від одного.

На першому кроці кореневий pom.xml було перетворено на батьківський POM з типом packaging pom. Він більше не збирає прикладну програму самостійно, а об’єднує модулі common, order-service і catalog-service. У секції modules перелічені всі три модулі. У кореневому POM залишені спільні властивості версії Java, кодування, MapStruct, Lombok, WireMock і Jacoco, а також спільні налаштування компілятора, тестів, покриття коду та Spring Boot. Це дає можливість усім дочірнім модулям використовувати однакові правила збірки.

На другому кроці створено модуль common. Він має власний pom.xml і не містить головного класу Spring Boot, контролерів, репозиторіїв або бізнес-логіки. У цей модуль винесено клас ToyResponse, тому що це DTO, який може використовуватися різними сервісами. Модуль common збирається у власний артефакт common-1.0.0-SNAPSHOT і підключається як залежність до order-service та catalog-service. Завдяки цьому спільний DTO не дублюється у коді сервісів.

На третьому кроці попередній код магазину було перенесено у модуль order-service. У ньому залишилися головний клас ToyStoreApplication, контролери замовлень, іграшок і токенів, сервіси, доменні моделі, репозиторії, конфігурація безпеки, асинхронна обробка, Quartz-задачі, Liquibase-ресурси та всі попередні тести. Зовнішня поведінка цього сервісу збережена. Для нього створено власний pom.xml з усіма залежностями, потрібними для JPA, Web, AOP, OAuth2, Actuator, Validation, Quartz, MariaDB, Liquibase, MapStruct, тестів і WireMock. У власному application.yml сервіс працює на порту 8081 і має назву toy-store-service.

На четвертому кроці для order-service створено окремий Dockerfile. Він використовує Java 17 runtime, копіює зібраний order-service-1.0.0-SNAPSHOT.jar у контейнер, відкриває порт 8081 і запускає застосунок командою java -jar. Таким чином order-service можна пакувати та запускати незалежно від інших модулів.

На п’ятому кроці створено окремий модуль catalog-service. Це самостійний Spring Boot застосунок зі своїм класом CatalogServiceApplication, власним pom.xml, власним application.yml, власним тестом і власним Dockerfile. Він не залежить від order-service. Єдиною спільною залежністю між ними є модуль common. Catalog service використовує DTO ToyResponse з common і має окремий контролер CatalogController.

У CatalogController додано endpoint GET /api/catalog/health. Він повертає текст catalog-service is running і дає змогу швидко перевірити, що саме каталог запущений. Також додано endpoint GET /api/catalog/toys. Він повертає демонстраційний список іграшок у форматі спільного DTO ToyResponse. Каталог працює на порту 8082, тому його порт не перетинається з портом order-service. У його application.yml задано назву toy-catalog-service та окремий файл логу logs/toy-catalog-service.log.

Для catalog-service додано CatalogControllerTest. Тест перевіряє доступність health endpoint і перевіряє, що endpoint зі списком іграшок повертає поля sku та name з DTO, який прийшов із common-модуля. Це підтверджує не тільки роботу контролера, а й підключення спільного модуля як Maven-залежності.

На шостому кроці для catalog-service створено Dockerfile. Він використовує Java 17 runtime, копіює catalog-service-1.0.0-SNAPSHOT.jar, відкриває порт 8082 і запускає сервіс незалежно від order-service. У результаті кожен прикладний модуль має свій головний клас, свій POM, свої ресурси, свої тести та свій Dockerfile.

Після зміни структури було перевірено батьківські зв’язки. У common, order-service і catalog-service секція parent посилається на ../pom.xml. Це означає, що кожен дочірній POM використовує кореневий POM як спільний parent. При цьому кореневий POM використовує Spring Boot parent і не є parent одного конкретного сервісу. Така схема дозволяє об’єднати модулі одним процесом збірки та не робить один прикладний сервіс батьківським для іншого.

Залежності між модулями направлені тільки від сервісів до common. order-service залежить від common, catalog-service залежить від common, а залежності order-service від catalog-service і catalog-service від order-service немає. Спільний модуль містить тільки спільний DTO, тому він не містить сервісної логіки та не створює циклічних залежностей.

Після внесення змін було запущено mvn validate з кореневої директорії. Команда успішно прочитала структуру reactor і побачила чотири проєкти: кореневий агрегатор, common, order-service і catalog-service.

Після цього було запущено повну команду mvn verify. Maven спочатку зібрав common, потім order-service, а потім catalog-service. У order-service успішно пройшли всі 93 наявні тести. У catalog-service успішно пройшли 2 нові тести. Загальний процес завершився зі статусом BUILD SUCCESS. Для order-service та catalog-service також виконалася перевірка покриття Jacoco, і мінімальне обмеження покриття було виконано в обох модулях.

Під час першої повної збірки в catalog-service виникла помилка запуску тестового JVM, тому що загальне налаштування Surefire очікувало шлях до Mockito-агента, а каталог не мав плагіна, який обчислює цей шлях. Це було виправлено додаванням тестової залежності Mockito та maven-dependency-plugin у catalog-service. Після цього модульний запуск mvn -pl catalog-service -am verify завершився успішно, а повторна повна перевірка підтвердила працездатність усієї структури.

Для ручної перевірки після збірки order-service можна запускати з order-service/target командою java -jar order-service-1.0.0-SNAPSHOT.jar. Він буде доступний на порту 8081. Catalog service можна запускати з catalog-service/target командою java -jar catalog-service-1.0.0-SNAPSHOT.jar. Він буде доступний на порту 8082. Після запуску каталогу його роботу можна перевірити через GET http://localhost:8082/api/catalog/health, а список іграшок отримати через GET http://localhost:8082/api/catalog/toys.

У підсумку один великий Maven-проєкт тепер має кореневий агрегатор, спільний модуль DTO та два окремі сервіси. Кожен сервіс має власний життєвий цикл збірки, власну точку входу, власні ресурси, власний порт, власні тести та власний Dockerfile. Спільні класи не копіюються між сервісами, а підключаються через common. Збірка з кореня, тести та перевірка покриття проходять успішно.

Звіт до лабораторної роботи 8

У цій лабораторній роботі до двох сервісів додано обмін повідомленнями через RabbitMQ. Для бізнес-події використано момент успішного оформлення замовлення. Після створення замовлення order-service створює OrderPlacedEvent зі стабільним ідентифікатором події, ідентифікатором замовлення, користувачем, сумою та часом створення.

Подію OrderPlacedEvent винесено у модуль common. Завдяки цьому order-service і catalog-service використовують спільний DTO для серіалізації та десеріалізації повідомлення, але не імпортують класи один одного. У common немає HTTP-клієнтів, RabbitMQ-конфігурації або бізнес-логіки сервісів.

У order-service додано RabbitMessagingConfiguration. Він створює durable topic exchange toy-store.order-events, JSON message converter і RabbitTemplate. OrderPlacedEventPublisher отримує події через ApplicationEventPublisher, але відправляє повідомлення в RabbitMQ лише через метод, позначений @TransactionalEventListener з фазою AFTER_COMMIT. Тому якщо транзакція оформлення замовлення завершується помилкою або rollback, публікація події не виконується. Якщо замовлення успішно зафіксоване, повідомлення відправляється з routing key order.placed і позначається як persistent.

У OrderService подія створюється після збереження замовлення та до завершення транзакції. Саме application event не є прямим викликом catalog-service. Після commit Spring передає його OrderPlacedEventPublisher, який працює з RabbitTemplate. Це відокремлює транзакцію бази даних від споживача повідомлення і не створює прямої HTTP-залежності між сервісами.

У catalog-service додано власну RabbitMessagingConfiguration. Вона оголошує такий самий durable exchange, durable queue toy-store.catalog.order-placed і binding з routing key order.placed. OrderPlacedEventConsumer слухає queue через @RabbitListener. Коли подія приходить, consumer записує її у ProcessedOrderEventStore. Для демонстрації результату додано GET /api/catalog/order-events, який повертає вже оброблені події.

ProcessedOrderEventStore використовує eventId як ключ. Метод recordIfNew додає подію лише якщо такого UUID ще не було. Якщо RabbitMQ повторно доставить те саме повідомлення, consumer не виконає бізнес-обробку вдруге, а запише в лог, що дубль проігнорований. Це забезпечує ідемпотентність споживача.

У compose.yml додано контейнер rabbitmq на базі rabbitmq:3.13-management. Для нього описані порти 5672 для AMQP і 15672 для management interface, а також volume toy-store-rabbitmq-data для збереження стану брокера. У compose також додані order-service і catalog-service. Усередині Docker вони отримують змінну TOY_STORE_RABBITMQ_HOST зі значенням rabbitmq, тобто використовують ім’я сервісу з compose, а не localhost.

У application.yml обох сервісів адреса брокера читається зі змінних середовища TOY_STORE_RABBITMQ_HOST, TOY_STORE_RABBITMQ_PORT, TOY_STORE_RABBITMQ_USERNAME і TOY_STORE_RABBITMQ_PASSWORD. Якщо сервіс запускається з IDE, використовуються значення за замовчуванням, зокрема localhost. Для exchange, routing key і queue також задані змінні середовища з локальними default-значеннями.

Поведінку producer перевірено в OrderPlacedEventPublisherTest. Тест перевіряє, що подія відправляється у потрібний exchange з routing key order.placed, а також перевіряє наявність фази AFTER_COMMIT. Поведінку consumer перевірено в OrderPlacedEventConsumerTest. У ньому одна й та сама подія передається двічі, але ProcessedOrderEventStore залишає тільки один запис.

Роботу нового спільного event flow додатково перевірено в OrderServiceTest. Під час успішного оформлення замовлення тест перевіряє виклик ApplicationEventPublisher. При цьому помилки оформлення замовлення завершують метод раніше, тому подія не створюється і не публікується.

Після змін було виконано mvn -pl order-service -am test. У order-service пройшли 95 тестів без failures та errors. Потім виконано mvn -pl catalog-service -am test. У catalog-service пройшли 3 тести без failures та errors, включно з MVC-тестами контролера та тестом повторної доставки події.

Для запуску брокера використовується команда docker compose up -d rabbitmq. Для запуску всіх контейнерів після попередньої Maven-збірки використовується docker compose up --build. Перевірити оброблені повідомлення можна через GET http://localhost:8082/api/catalog/order-events. RabbitMQ management interface доступний на http://localhost:15672 з обліковими даними guest і guest.

У результаті order-service та catalog-service обмінюються бізнес-подіями через RabbitMQ, не викликають один одного через HTTP і не залежать від класів один одного. Повідомлення зберігається в durable queue, тому producer може успішно опублікувати подію, коли consumer тимчасово вимкнений. Після запуску consumer накопичені повідомлення будуть отримані, а повторна доставка тієї самої події не створить дубль завдяки перевірці eventId.

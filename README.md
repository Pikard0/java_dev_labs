Звіт для лабараторної роботи 5
1. Мета та завдання роботи
Метою лабораторної роботи є проєктування та впровадження механізмів асинхронної обробки у веб-застосунку Toy Store. Це дозволяє усунути блокування клієнтських HTTP-запитів повільними побічними операціями, оптимізувати використання серверних ресурсів завдяки виділеному пулу потоків з обмеженими межами, а також гарантувати передбачувану поведінку у разі збоїв, таймаутів або високого навантаження.
Для досягнення мети було поставлено такі завдання:
Виділити в доменній області магазину 3 повільні операції: надсилання листа, формування PDF-квитанції та запит до зовнішньої служби доставки.
Налаштувати ThreadPoolTaskExecutor із фіксованими межами пулу, чергою очікування та префіксом імен потоків, зареєструвавши його через Spring AsyncConfigurer.
Реалізувати шаблон «Запустив і забув» (Fire-and-forget) для надсилання email із фіксацією помилок через AsyncUncaughtExceptionHandler.
Реалізувати конкурентне виконання двох повільних викликів із типом повернення CompletableFuture<T> та їх комбінування через thenCombine в єдиний агрегований результат.\
Забезпечити гарантоване обмеження часу очікування асинхронних операцій (таймаути) та повернення безпечного fallback-результату.
Обробити ситуацію вичерпання ресурсів виконавця (TaskRejectedException), не допускаючи виникнення випадкових помилок HTTP 500.
Розробити набір автоматизованих тестів, що підтверджують виконання всіх BDD-сценаріїв лабораторної роботи.
2. Вибір та обґрунтування повільних операцій доменної логіки
У доменній моделі інтернет-магазину іграшок Toy Store взаємодія користувача із замовленням супроводжується низкою ресурсомістких і зовнішніх операцій. Якщо виконувати їх синхронно в основному потоці веб-сервера Tomcat, час відповіді суттєво зростає, а потік веб-сервера блокується, знижуючи пропускну здатність усієї системи.
Операція	Тип операції	Причина повільності	Асинхронний патерн
Надсилання email-підтвердження	OrderNotificationService	Встановлення SMTP/TLS з'єднання, зовнішній поштовий шлюз (~500 мс)	Fire-and-forget (@Async void)
Генерація PDF-квитанції	OrderReceiptService	Рендеринг друкованої форми замовлення, накладання шрифтів (~500 мс)	CompletableFuture<OrderReceipt>
Запит оцінки термінів доставки	DeliveryEstimateService	Мережевий виклик HTTP до сторонньої кур'єрської API (~500 мс)	CompletableFuture<DeliveryEstimate>

Для тестування та демонстрації реалізовано компонент AsyncWorkDelay, який у звичайному режимі роботи застосунку симулює реалістичну затримку (SimulatedAsyncWorkDelay), а в тестах дозволяє точно синхронізувати потоки через CountDownLatch (BlockingAsyncWorkDelay) без використання крихких Thread.sleep().
3. Архітектура асинхронної підсистеми
Асинхронна архітектура проєкту розроблена з дотриманням принципів слабкої зв'язності (Loose Coupling) та єдиної відповідальності (Single Responsibility). Контролер делегує завдання фасадному сервісу OrderService, який взаємодіє з асинхронними компонентами.
Клас / Компонент	Шлях у проєкті	Призначення та відповідальність
AsyncConfiguration	com.java.toystore.configuration	Налаштування пулу потоків, реалізація AsyncConfigurer.
AsyncTaskProperties	com.java.toystore.configuration	Типізовані конфігураційні властивості toy-store.async.*.
OrderNotificationService	com.java.toystore.async	Фонове надсилання сповіщень покупцям (fire-and-forget).
OrderReceiptService	com.java.toystore.async	Асинхронне генерування квитанцій замовлення.
DeliveryEstimateService	com.java.toystore.async	Асинхронний запит до зовнішньої логістичної системи.
OrderFulfillmentService	com.java.toystore.async	Координація асинхронних викликів, thenCombine, orTimeout, fallback.
OrderService	com.java.toystore.service	Доменний фасад, інтеграція асинхронних сервісів у бізнес-процес.
GlobalExceptionHandler	com.java.toystore.exception	Централізована обробка помилок та мапінг TaskRejectedException.

Важливою архітектурною перевагою є ізоляція асинхронних викликів: викликаючий код не залежить від фізичної реалізації багатопотоковості та оперує декларативними контрактами CompletableFuture.
4. Налаштування пулу потоків ThreadPoolTaskExecutor та AsyncConfigurer
Використання дефолтного SimpleAsyncTaskExecutor у Spring Boot є небезпечним для production, оскільки він створює новий потік операційної системи на кожне завдання без обмежень, що при піковому навантаженні спричиняє OutOfMemoryError. Тому в проєкті сконфігуровано керований ThreadPoolTaskExecutor.
Параметри пулу потоків винесені у файл application.yml і підтримують динамічне переналаштування:
toy-store:
  async:
    core-pool-size: 2
    max-pool-size: 4
    queue-capacity: 8
    thread-name-prefix: toy-store-async-
    simulated-delay-millis: 500
    result-timeout-millis: 2000
Клас AsyncConfiguration реалізує інтерфейс AsyncConfigurer, завдяки чому сконфігурований пул стає основним асинхронним виконавцем програми за замовчуванням:
@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean("toyStoreTaskExecutor")
    public ThreadPoolTaskExecutor toyStoreTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        return executor;
    }

    @Override
    public TaskExecutor getAsyncExecutor() {
        return toyStoreTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncUncaughtExceptionHandler();
    }
}
Префікс 'toy-store-async-' дає змогу легко відстежувати походження потоків у логах застосунку та профайлерах.
5. Реалізація моделі «Запустив і забув» (Fire-and-forget)
При створенні замовлення користувач не повинен очікувати, поки сервер підключиться до поштового шлюзу і відправить email. Метод надсилання листа має тип повернення void і помічений анотацією @Async:
@Async("toyStoreTaskExecutor")
public void sendOrderConfirmation(String customerUsername, OrderResponse order) {
    delay.waitForSlowOperation("order confirmation email");
    if (customerUsername == null || customerUsername.isBlank()) {
        throw new IllegalArgumentException("Cannot send an order confirmation without a recipient");
    }
    log.info("Order confirmation for order {} was sent to {} on {}",
            order.getId(), customerUsername, Thread.currentThread().getName());
}
У OrderService.placeOrder(...) виклик сервісу сповіщень відбувається одразу після збереження замовлення у базі даних. Контролер повертає відповідь 201 Created за кілька мілісекунд, тоді як надсилання листа завершується у фоновому пулі потоків.
Оскільки методи void не мають Future для повернення помилки викликаючому коду, будь-який виняток у такому методі міг би безслідно зникнути. Для запобігання цьому реалізовано LoggingAsyncUncaughtExceptionHandler, який фіксує ім'я методу, передані параметри та стек-трейс у журналі помилок:
private static class LoggingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable exception, Method method, Object... parameters) {
        log.error("Fire-and-forget async method {} failed with parameters {}",
                method.toGenericString(), parameters, exception);
    }
}
6. Паралельне виконання та об'єднання результатів (thenCombine)
Для операції попереднього перегляду виконання замовлення (Order Fulfillment Preview) викликаючому коду потрібні дані з двох незалежних джерел:
Згенерована PDF-квитанція (OrderReceiptService, повертає CompletableFuture<OrderReceipt>).
Розрахована дата доставки (DeliveryEstimateService, повертає CompletableFuture<DeliveryEstimate>).
Замість послідовного виклику (який забрав би 500 мс + 500 мс = 1000 мс) сервіс OrderFulfillmentService запускає обидва методи одночасно, після чого об'єднує їх результати за допомогою неблокуючого оператора thenCombine:
public CompletableFuture<OrderFulfillmentPreview> buildPreview(OrderResponse order) {
    try {
        CompletableFuture<OrderReceipt> receiptFuture = orderReceiptService.generateReceiptPdf(order);
        CompletableFuture<DeliveryEstimate> deliveryFuture = deliveryEstimateService.requestEstimate(order);

        return receiptFuture
                .thenCombine(deliveryFuture, (receipt, delivery) -> new OrderFulfillmentPreview(
                        order.getId(),
                        receipt.pdfFileName(),
                        delivery.deliveryDate(),
                        true,
                        "Fulfillment details are ready"
                ))
                .orTimeout(properties.getResultTimeoutMillis(), TimeUnit.MILLISECONDS)
                .handle((preview, exception) -> {
                    if (exception == null) {
                        return preview;
                    }
                    log.warn("Fulfillment details for order {} are unavailable", order.getId(), exception);
                    return OrderFulfillmentPreview.unavailable(
                            order.getId(),
                            "Fulfillment details are temporarily unavailable. Please try again later."
                    );
                });
    } catch (TaskRejectedException exception) {
        ...
    }
}
Завдяки одночасному запуску загальний час очікування скорочується майже вдвічі і дорівнює часу найповільнішої з двох операцій (~500 мс замість 1000 мс).
7. Відмовостійкість, таймаути та обробка переповнення черги
Асинхронний код проєкту побудовано за принципом «Graceful Degradation» (поступове зниження функціональності без аварійного завершення). Реалізовано три рівні захисту:
1. Захист від збоїв через .handle(...): якщо генерація PDF або запит доставки викидають виняток (наприклад, сторонній сервіс тимчасово недоступний), метод .handle() перехоплює помилку, логує її та повертає коректний об'єкт OrderFulfillmentPreview зі статусом available=false та зрозумілим повідомленням для користувача. Запит не завершується HTTP 500.
2. Гарантовані таймаути: у пайплайні використано .orTimeout(2000, TimeUnit.MILLISECONDS), а на рівні виклику OrderService застосовано get(timeout, TimeUnit.MILLISECONDS) із перехопленням TimeoutException. Якщо операція затримується понад встановлений ліміт, очікування автоматично припиняється.
3. Захист від перевантаження (TaskRejectedException): коли черга завдань (8) і всі потоки (4) заповнені, пул потоків за правилом AbortPolicy відхиляє нові задачі.
У OrderFulfillmentService виняток TaskRejectedException перехоплюється локально і повертає fallback-відповідь «Fulfillment processing is busy. Please try again later.». Якщо ж TaskRejectedException виникає на рівні контролера, він перехоплюється в GlobalExceptionHandler і повертає статус HTTP 503 Service Unavailable замість помилки 500:
@ExceptionHandler(TaskRejectedException.class)
public ResponseEntity<ApplicationErrorResponse> handle(TaskRejectedException ex) {
    log.warn("Async task rejected: {}", ex.getMessage());
    return new ResponseEntity<>(
            new ApplicationErrorResponse("Async processing is busy. Please try again later."),
            HttpStatus.SERVICE_UNAVAILABLE
    );
}
8. Тестування та результати верифікації
Надійність асинхронної обробки повністю підтверджена автоматизованими модульними та інтеграційними тестами. Усі сценарії лабораторної роботи перевіряються окремими тест-кейсами:
Сценарій лабораторної роботи	Тестовий клас	Суть перевірки та результат
Повільний метод не блокує викликаючий код	OrderAsyncEffectsIntegrationTest	Метод placeOrder повертає керування за < 100 мс, сповіщення триває у фоновому потоці toy-store-async-*.
Виконавець із заданими межами та префіксом	AsyncConfigurationTest	Перевірка core=1, max=2, queue=3, threadNamePrefix='toy-store-async-'.
Два виклики виконуються одночасно	OrderAsyncEffectsIntegrationTest	За допомогою CountDownLatch підтверджено одночасний старт двох потоків до звільнення бар'єра.
Невдалий виклик не ламає програму	OrderFulfillmentServiceTest	При failedFuture повертається OrderFulfillmentPreview.unavailable() без прокидання exception.
Фіксація помилок у void-методі	AsyncConfigurationTest	AsyncUncaughtExceptionHandler коректно приймає виняток і логує його.
Очікування обмежене в часі (таймаут)	OrderFulfillmentServiceTest / OrderServiceTest	Future, який не завершується вчасно, переривається за таймаутом і повертає fallback.
Обробка переповнення пулу (503 замість 500)	AsyncConfigurationTest / GlobalExceptionHandlerTest	TaskRejectedException генерується при переповненні та повертає HTTP 503.

Запуск повної збірки проєкту за допомогою Maven підтвердив успішне проходження всіх тестів:
[INFO] Results:
[INFO] Tests run: 93, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
Плагін JaCoCo підтвердив повне виконання нормативів покриття коду тестами без порушень правил якості.

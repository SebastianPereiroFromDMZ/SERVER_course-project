# Здравствуйте.
Данная программа-сервер предназначенна для подключения клиентов к чату и отправки сообщения от отправившего сообщение
клиента к всем другим присоединенным к серверу клиентам.  
  


Код представлен классом Server, вложенным в него классом ConnectionHandler и классом Logger  

### Класс Server
  
В классе сервера имеется 6 полей:
 - private ArrayList<ConnectionHandler> connections: список для хранения присоединенных клиентов
 - private ServerSocket server: сокета сервера
 - private boolean done: булевское выражение для выхода из цикла метода ран
 - private ExecutorService pool: пул потоков
 - private int clientCount : кол-во соединений, используется во вложенном классе
 - Logger logger: логера, используется во вложенном классе
  
Сервер имплементирует интерфейс Раннабл, и в нем переопределен метод ран:  
```java
@Override
    public void run() {
        try {
            server = new ServerSocket(getPort());
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }
```
в ктором создается сокет сервера с портом из
метода гетпорт, далее создается фабрика с пулом потоков, которая создает потоки по мере необходимости, 
и переиспользует неактивные потоки. Далее открывается цикл с условием булевского ДАН. Внутри цикла создается клиентский сокет 
когда естественно присоединится клиент, а до тех пор сервер находится в ожидании присоединения. После того как клиент присоединился
создается новый обработчик соединения (клиент) в конструктр которого передается клиентский сокет. Добавляется в скписок 
и просим пул выполнить задачу.  
В случае исключения вызывается метод выключения в котором обрывается цикл метода ран, пул потоков выключается, сокет сервера закрывается,
так же закрываются все сокеты присоединенных клиентов.  
  
Так же естественно имеется метод отправки сообщения от клиента всем клиентам.  
  
Метод входа в программу: создаем обьект сервера и запускаем его.
  
### Вложенный класс ConnectionHandler  
Имеется 4 поля:  
 - private Socket client: клиентский сокет  
 - private BufferedReader in: считывающий входящий поток символов из клиентского сокета
 - private PrintWriter out: отправляет сообщения конкретно этому клиенту
 - private String nickname: никнэйм присоединенного этого клиента  
  
Обработчик соединений так же имплементирует интерфейс раннабл и переопределяем метод ран:  
```java
@Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                logger = new Logger();
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected!");
                logger.log(nickname + " connected!");
                clientCount++;
                broadcast(nickname + " joined to chat!");
                broadcast("Number of people in the chat " + clientCount);
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            logger.log(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to " + messageSplit[1]);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/exit")) {
                        broadcast(nickname + " left the chat");
                        logger.log(nickname + " left the chat");
                        clientCount--;
                        broadcast("Number of people in the chat " + clientCount);
                        shutdown();
                    } else {
                        broadcast(nickname + ": " + message);
                        logger.log(nickname + ": " + message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
```
  
в этом переопределенном методе:  
создаем новые потоки привязывая их к клиентскому сокету, создаем новый логер, отправляем клиенту сообщение ввести никнейм,
записываем в поле никнейм ту строку которую нам передал клиент, выводим в консоль сервера о присоединении нового клиента 
с введеным им ником, логгируем, увеличиваем сщетчик клиентов, рассылаем всем клиентам и присоединение нового и исзменении сщетчика.
Создаем обьект строки. Открываем цикл с условием и одновременным присвоением обьекту строки принятое сообщение от клиента,
условие для продолжение цикла то что сообщение от клинта не равно налл(тоеть оно есть). В цикле есть несколько ифов в зависимости какое
сообщение отправил клиент:
 - 1: о смене ника, где клиент меняет никнейм и это действие рассылается всем клинтам чата, логируется  
 - 2: выход из чата, где тоже рассылается все сообщение о выходе этого клиента из чата, логируется, обновляется сщетчикк клиентов,
 вызывается метод выключения клиента :закрываются потоки ввода, вывода, закрывается клиентский сокет
 - 3: обычное сообщение которое рассылается всем клиентам  
  
В случае возникновения исключения также вызывается метод выключения клиента.  
  
Метод отправки сообщение отправляет сообщение клиенту от клиента, используется в цикле фор для отправки всем клиентам в другом методе.  
  




  

  




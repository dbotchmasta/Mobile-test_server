package server;

import structures.entities.PHClients;
import structures.entities.PHCounts;
import structures.goods.AccountingOfOptions;
import structures.operations.PHConnection;
import structures.operations.PHCount;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {
    
    private Socket s;
    
    private DataInputStream inStream;
    private DataOutputStream outStream;
    
    private Storage storage;
    
    public Server(Storage storage, Socket s) {
        this.storage = storage;
        this.s = s;
    }
    
    @Override
    public void run() {
        try {
            inStream = new DataInputStream(s.getInputStream());
            outStream = new DataOutputStream(s.getOutputStream());

            String request, response;
            while ((request = inStream.readUTF()) != null) {
                System.out.println("Запрос: " + request);
                response = getResponse(request);
                outStream.writeUTF(response);
            }

            System.out.println("Клиент отключился");
            inStream.close();
            outStream.close();
            s.close();
        } catch (IOException ex) {
            System.err.println("Соединение с клиентом прервано");
        }
    }
    
    private synchronized String getResponse(String request) throws IOException {
        switch (request) {
            case ServerCommands.GET_LIST_OF_ALL_OPTIONS:
                return new Gson().toJson(storage.getListOfAllOptions());
                
            case ServerCommands.GET_LIST_OF_OPTIONS_BY_KIND:
                String kind = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + kind);
                return new Gson().toJson(storage.getListOfOptionsByKind(kind));
                
            case ServerCommands.GET_LIST_OF_OPTIONS_BY_COUNT:
                int countId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + countId);
                return new Gson().toJson(storage.getListOfOptionsByCount(countId));
                
            case ServerCommands.GET_LIST_OF_OPTIONS_BY_CLIENT:
                int clientId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + clientId);
                return new Gson().toJson(storage.getListOfOptionsByClient(clientId));
                
            case ServerCommands.GET_LIST_OF_COUNTS_BY_DATE:
                String gsDateBegin = inStream.readUTF();
                System.out.println("Запрос.аргумент1: " + gsDateBegin);
                String gsDateEnd = inStream.readUTF();
                System.out.println("Запрос.аргумент2: " + gsDateEnd);
                return new Gson().toJson(storage.getListOfCountsByDateOpen(
                        new Gson().fromJson(gsDateBegin, Date.class),
                        new Gson().fromJson(gsDateEnd, Date.class)));
                
            case ServerCommands.GET_LIST_OF_CONNECTIONS_BY_DATE:
                gsDateBegin = inStream.readUTF();
                System.out.println("Запрос.аргумент1: " + gsDateBegin);
                gsDateEnd = inStream.readUTF();
                System.out.println("Запрос.аргумент2: " + gsDateEnd);
                return new Gson().toJson(storage.getListOfConnectionsByDateOpen(
                        new Gson().fromJson(gsDateBegin, Date.class),
                        new Gson().fromJson(gsDateEnd, Date.class)));
                
            case ServerCommands.GET_LIST_OF_ALL_COUNTS:
                return new Gson().toJson(storage.getListOfAllCounts());
                
            case ServerCommands.GET_LIST_OF_ALL_CLIENTS:
                return new Gson().toJson(storage.getListOfAllClients());
                
            case ServerCommands.GET_LIST_OF_COUNTS_BY_OPTIONS:
                countId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + countId);
                return new Gson().toJson(storage.getListOfCountsByOptions(countId));
                
            case ServerCommands.GET_LIST_OF_CLIENTS_BY_OPTIONS:
                clientId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + clientId);
                return new Gson().toJson(storage.getListOfClientsByOptions(clientId));
                
            case ServerCommands.COMPLETE_COUNT:
                String gsSupply = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + gsSupply);
                return storage.completeCount(new Gson().fromJson(gsSupply, PHCount.class));
                
            case ServerCommands.COMPLETE_CONNECTION:
                String gsConnection = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + gsConnection);
                return storage.completeConnection(new Gson().fromJson(gsConnection, PHConnection.class));
                
            case ServerCommands.ADD_ACCOUNTING_OF_OPTIONS:
                String gsAccOfOptions = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + gsAccOfOptions);
                return storage.addAccountingOfOptions(new Gson().fromJson(gsAccOfOptions, AccountingOfOptions.class));
                
            case ServerCommands.UPD_ACCOUNTING_OF_OPTIONS:
                gsAccOfOptions = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + gsAccOfOptions);
                return storage.updAccountingOfOptions(new Gson().fromJson(gsAccOfOptions, AccountingOfOptions.class));
                
            case ServerCommands.DEL_ACCOUNTING_OF_OPTIONS:
                int optionsId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + optionsId);
                return storage.delAccountingOfOptions(optionsId);
                
            case ServerCommands.ADD_COUNT:
                String count = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + count);
                return storage.addCount(new Gson().fromJson(count, PHCounts.class));
                
            case ServerCommands.ADD_CLIENT:
                String client = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + client);
                return storage.addClient(new Gson().fromJson(client, PHClients.class));
                
            case ServerCommands.ADD_COUNT_FOR_OPTIONS:
                countId = inStream.readInt();
                System.out.println("Запрос.аргумент1: " + countId);
                optionsId = inStream.readInt();
                System.out.println("Запрос.аргумент2: " + optionsId);
                return storage.addCountForOptions(countId, optionsId);
                
            case ServerCommands.ADD_CLIENT_FOR_OPTIONS:
                clientId = inStream.readInt();
                System.out.println("Запрос.аргумент1: " + clientId);
                optionsId = inStream.readInt();
                System.out.println("Запрос.аргумент2: " + optionsId);
                return storage.addClientForOptions(clientId, optionsId);
                
            case ServerCommands.UPD_COUNT:
                count = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + count);
                return storage.updCount(new Gson().fromJson(count, PHCounts.class));
                
            case ServerCommands.UPD_CLIENT:
                client = inStream.readUTF();
                System.out.println("Запрос.аргумент: " + client);
                return storage.updClient(new Gson().fromJson(client, PHClients.class));
                
            case ServerCommands.DEL_COUNT:
                countId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + countId);
                return storage.delCount(countId);
                
            case ServerCommands.DEL_CLIENT:
                clientId = inStream.readInt();
                System.out.println("Запрос.аргумент: " + clientId);
                return storage.delClient(clientId);    
                
            case ServerCommands.DEL_COUNT_FOR_OPTIONS:
                countId = inStream.readInt();
                System.out.println("Запрос.аргумент1: " + countId);
                optionsId = inStream.readInt();
                System.out.println("Запрос.аргумент2: " + optionsId);
                return storage.delCountForOptions(countId, optionsId);
                
            case ServerCommands.DEL_CLIENT_FOR_OPTIONS:
                clientId = inStream.readInt();
                System.out.println("Запрос.аргумент1: " + clientId);
                optionsId = inStream.readInt();
                System.out.println("Запрос.аргумент2: " + optionsId);
                return storage.delClientForOptions(clientId, optionsId);
                
            default:
                return "Команда не распознана";
        }
    }
    
    public static void main(String[] args) {
        Storage storage = new Storage();
        storage.readDataFromFiles();
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            synchronized(storage) {
                storage.saveDataToFiles();
            }
        }, 1, 1, TimeUnit.MINUTES);
        
        ServerSocket ss;
        try {
            System.out.println("Сервер запущен");
            ss = new ServerSocket(5000);
            while (true) {
                Socket s = ss.accept();
                System.out.println("Клиент подключился");
                new Thread(new Server(storage, s)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            storage.saveDataToFiles();
        }
    }
}

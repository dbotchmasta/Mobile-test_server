package server;

import structures.entities.PHClients;
import structures.entities.PHCounts;
import structures.goods.AccountingOfOptions;
import structures.operations.PHCount;
import structures.operations.PHConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Storage {
    
    private final File datafileListOfAccountingOfOptions = new File("datafile_list_of_accounting_of_options.txt");
    private final File datafileListOfCounts = new File("datafile_list_of_counts.txt");
    private final File datafileListOfClients = new File("datafile_list_of_clients.txt");
    private final File datafileListOfCount = new File("datafile_list_of_count.txt");
    private final File datafileListOfConnections = new File("datafile_list_of_connections.txt");
    
    private Map<Integer, AccountingOfOptions> listOfAccountingOfOptions = new HashMap();
    private Map<Integer, PHCounts> listOfCounts = new HashMap();
    private Map<Integer, PHClients> listOfClients = new HashMap();
    private List<PHCount> listOfCount = new ArrayList();
    private List<PHConnection> listOfConnection = new ArrayList();
     
    private final int MAX_TOTAL_QUANTITY = 1000;
    
    public List<AccountingOfOptions> getListOfAllOptions() {
        List<AccountingOfOptions> result = new ArrayList();
        listOfAccountingOfOptions.entrySet().forEach((item) -> {
            result.add(item.getValue());
        });
        return result;
    }
    
    public List<AccountingOfOptions> getListOfOptionsByKind(String kind) {
        List<AccountingOfOptions> result = new ArrayList();
        for (Map.Entry<Integer, AccountingOfOptions> item : listOfAccountingOfOptions.entrySet()) {
            if (item.getValue().getOptions().getKind().toString().equalsIgnoreCase(kind)
                    || item.getValue().getOptions().getKind().toString().toLowerCase().startsWith(kind.toLowerCase())) {
                result.add(item.getValue());
            }
        }
        return result;
    }
    
    public List<AccountingOfOptions> getListOfOptionsByCount(int countId) {
        List<AccountingOfOptions> result = new ArrayList();
        for (Map.Entry<Integer, AccountingOfOptions> item : listOfAccountingOfOptions.entrySet()) {
            if (item.getValue().hasCount(countId)) {
                result.add(item.getValue());
            }
        }
        return result;
    }
    
    public List<AccountingOfOptions> getListOfOptionsByClient(int clientId) {
        List<AccountingOfOptions> result = new ArrayList();
        for (Map.Entry<Integer, AccountingOfOptions> item : listOfAccountingOfOptions.entrySet()) {
            if (item.getValue().hasClient(clientId)) {
                result.add(item.getValue());
            }
        }
        return result;
    }
    
    public List<PHCount> getListOfCountsByDateOpen(Date begin, Date end) {
        List<PHCount> result = new ArrayList();
        for (PHCount phcount : listOfCount) {
                if (phcount.getDateOpen().after(begin)
                        && phcount.getDateOpen().before(end)
                        || phcount.getDateOpen().equals(end)
                        || phcount.getDateOpen().equals(end)) {
                    result.add(phcount);
                }
            }
        return result;
    }
    
    public List<PHConnection> getListOfConnectionsByDateOpen(Date begin, Date end) {
        List<PHConnection> result = new ArrayList();
        for (PHConnection phconnection : listOfConnection) {
                if (phconnection.getDateOpen().after(begin)
                        && phconnection.getDateOpen().before(end)
                        || phconnection.getDateOpen().equals(end)
                        || phconnection.getDateOpen().equals(end)) {
                    result.add(phconnection);
                }
            }
        return result;
    }
    
    public List<PHCounts> getListOfAllCounts() {
        List<PHCounts> result = new ArrayList();
        listOfCounts.entrySet().forEach((item) -> {
            result.add(item.getValue());
        });
        return result;
    }
    
    public List<PHClients> getListOfAllClients() {
        List<PHClients> result = new ArrayList();
        listOfClients.entrySet().forEach((item) -> {
            result.add(item.getValue());
        });
        return result;
    }
    
    public List<PHCounts> getListOfCountsByOptions(int goodsId) {
        List<PHCounts> result = new ArrayList();
        List<Integer> listOfSuppliersID = listOfAccountingOfOptions.get(goodsId).getListOfCountsID();
        for (int i = 0; i < listOfSuppliersID.size(); i++) {
            result.add(listOfCounts.get(listOfSuppliersID.get(i)));
        }
        return result;
    }
    
    public List<PHClients> getListOfClientsByOptions(int optionsId) {
        List<PHClients> result = new ArrayList();
        List<Integer> listOfClientsID = listOfAccountingOfOptions.get(optionsId).getListOfClientsID();
        for (int i = 0; i < listOfClientsID.size(); i++) {
            result.add(listOfClients.get(listOfClientsID.get(i)));
        }
        return result;
    }
    
    public String completeCount(PHCount phcount) {
        PHCounts phcounts = listOfCounts.get(phcount.getEntityId());
        AccountingOfOptions accOptions = listOfAccountingOfOptions.get(phcount.getOptionsId());
        if (phcounts == null) {
            return "Cчет не найден";
        } else if (accOptions == null) {
            return "Тариф не найден";
        }
        listOfCount.add(phcount);
        accOptions.updateAccordingToCount(phcount);
        return "Подключение оформлено";
    }
    
    public String completeConnection(PHConnection phcon) {
        PHClients phclients = listOfClients.get(phcon.getEntityId());
        AccountingOfOptions accOptions = listOfAccountingOfOptions.get(phcon.getOptionsId());
        if (phclients == null) {
            return "Клиент не найден";
        } else if (accOptions == null) {
            return "Тариф не найден";
        }
        listOfConnection.add(phcon);
        accOptions.updateAccordingToConnection(phcon);
        return "Подключение оформлено";
    }
       
    public String addAccountingOfOptions(AccountingOfOptions accOfOptions) {
        if (!listOfAccountingOfOptions.containsKey(accOfOptions.getOptions().getId())) {
            listOfAccountingOfOptions.put(accOfOptions.getOptions().getId(), accOfOptions);
            return "Тариф добавлен";
        }
        return "Тариф с id " + accOfOptions.getOptions().getId() + " уже существует";
    }
    
    public String updAccountingOfOptions(AccountingOfOptions accOfOptions) {
        if (!listOfAccountingOfOptions.containsKey(accOfOptions.getOptions().getId())) {
            return "Тариф с id " + accOfOptions.getOptions().getId() + " не найден";
        }
        listOfAccountingOfOptions.put(accOfOptions.getOptions().getId(), accOfOptions);
        return "Информация о тарифе с id " + accOfOptions.getOptions().getId() + " обновлена";
    }
    
    public String delAccountingOfOptions(int optionsId) {
        if (!listOfAccountingOfOptions.containsKey(optionsId)) {
            return "Тариф с id " + optionsId + " не найден";
        }
        listOfAccountingOfOptions.remove(optionsId);
        return "Тариф с id " + optionsId + " удален";
    }
    
    public String addCount(PHCounts phcounts) {
        if (!listOfCounts.containsKey(phcounts.getId())) {
            listOfCounts.put(phcounts.getId(), phcounts);
            return "Счет добавлен";
        }
        return "Счет с id " + phcounts.getId() + " уже существует"; 
    }
    
    public String addClient(PHClients phclients) {
        if (!listOfClients.containsKey(phclients.getId())) {
            listOfClients.put(phclients.getId(), phclients);
            return "Счет добавлен";
        }
        return "Счет с id " + phclients.getId() + " уже существует"; 
    }
    
    public String addCountForOptions(int countId, int optionsId) {
        if (!listOfCounts.containsKey(countId)) {
            return "Счет с id " + countId + " не найден";
        } else if (!listOfAccountingOfOptions.containsKey(optionsId)) {
            return "Тариф с id " + optionsId + " не найден";
        }
        listOfAccountingOfOptions.get(optionsId).addCount(countId);
        return "Счет с id " + countId + " добавлен для тарифа с id " + optionsId;
    }
    
    public String addClientForOptions(int clientId, int optionsId) {
        if (!listOfClients.containsKey(clientId)) {
            return "Клиент с id " + optionsId + " не найден";
        } else if (!listOfAccountingOfOptions.containsKey(optionsId)) {
            return "Тариф с id " + optionsId + " не найден";
        }
        listOfAccountingOfOptions.get(optionsId).addClient(clientId);
        return "Клиент с id " + clientId + " добавлен для тарифа с id " + optionsId;
    }
    
    public String updCount(PHCounts phcounts) {
        if (!listOfCounts.containsKey(phcounts.getId())) {
            return "Счет с id " + phcounts.getId() + " не найден";
        }
        listOfCounts.put(phcounts.getId(), phcounts);
        return "Информация о счете с id " + phcounts.getId() + " обновлена";
    }
    
    public String updClient(PHClients phclients) {
        if (!listOfClients.containsKey(phclients.getId())) {
            return "Клиент с id " + phclients.getId() + " не найден";
        }
        listOfClients.put(phclients.getId(), phclients);
        return "Информация о клиенте с id " + phclients.getId() + " обновлена";
    }
    
    public String delCount(int phcountId) {
        if (!listOfCounts.containsKey(phcountId)) {
            return "Счет с id " + phcountId + " не найден";
        }
        listOfAccountingOfOptions.entrySet().forEach((item) -> {
            item.getValue().delCount(phcountId);
        });
        listOfCounts.remove(phcountId);
        return "Счет с id " + phcountId + " удален для всех тарифов";
    }
    
    public String delClient(int phclientId) {
        if (!listOfClients.containsKey(phclientId)) {
            return "Клиент с id " + phclientId + " не найден";
        }
        listOfAccountingOfOptions.entrySet().forEach((item) -> {
            item.getValue().delClient(phclientId);
        });
        listOfClients.remove(phclientId);
        return "Клиент с id " + phclientId + " удален для всех тарифов";
    }
    
    public String delCountForOptions(int phcountId, int optionsId) {
        if (!listOfCounts.containsKey(phcountId)) {
            return "Счет с id " + phcountId + " не найден";
        } else if (!listOfAccountingOfOptions.containsKey(optionsId)) {
            return "Тариф с id " + optionsId + " не найден";
        }
        listOfAccountingOfOptions.get(optionsId).delCount(phcountId);
        return "Счет с id " + phcountId + " удален для тарифа с id " + optionsId;
    }
    
    public String delClientForOptions(int clientId, int optionsId) {
        if (!listOfClients.containsKey(clientId)) {
            return "Клиент с id " + clientId + " не найден";
        } else if (!listOfAccountingOfOptions.containsKey(optionsId)) {
            return "Тариф с id " + optionsId + " не найден";
        }
        listOfAccountingOfOptions.get(optionsId).delClient(clientId);
        return "Клиент с id " + clientId + " удален для тарифа с id " + optionsId;
    }
    
    public void saveDataToFiles() {
        trySaveToFile(datafileListOfAccountingOfOptions, listOfAccountingOfOptions);
        trySaveToFile(datafileListOfCounts, listOfCounts);
        trySaveToFile(datafileListOfClients, listOfClients);
        trySaveToFile(datafileListOfCount, listOfCount);
        trySaveToFile(datafileListOfConnections, listOfConnection);
    }
    
    private void trySaveToFile(File file, Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String gsonList = gson.toJson(obj);
            writer.write(gsonList);
        } catch (IOException ex) {
            System.err.println("Ошибка записи в файл");
        }
    }
    
    public void readDataFromFiles() {
        Gson gson = new Gson();
        StringBuilder lines;
        
        lines = tryReadLinesFromFile(datafileListOfAccountingOfOptions);
        Type accountingOfOptionsMapType = new TypeToken<Map<Integer, AccountingOfOptions>>(){}.getType();
        listOfAccountingOfOptions = gson.fromJson(lines.toString(), accountingOfOptionsMapType);
        listOfAccountingOfOptions = listOfAccountingOfOptions == null ? new HashMap() : listOfAccountingOfOptions;
        
        lines = tryReadLinesFromFile(datafileListOfCounts);
        Type countsMapType = new TypeToken<Map<Integer, PHCounts>>(){}.getType();
        listOfCounts = gson.fromJson(lines.toString(), countsMapType);
        listOfCounts = listOfCounts == null ? new HashMap() : listOfCounts;
        
        lines = tryReadLinesFromFile(datafileListOfClients);
        Type clientsMapType = new TypeToken<Map<Integer, PHClients>>(){}.getType();
        listOfClients = gson.fromJson(lines.toString(), clientsMapType);
        listOfClients = listOfClients == null ? new HashMap() : listOfClients;

        lines = tryReadLinesFromFile(datafileListOfCount);
        Type countListType = new TypeToken<List<PHCount>>(){}.getType();
        listOfCount = gson.fromJson(lines.toString(), countListType);
        listOfCount = listOfCount == null ? new ArrayList() : listOfCount;
        
        lines = tryReadLinesFromFile(datafileListOfConnections);
        Type connectionsListType = new TypeToken<List<PHConnection>>(){}.getType();
        listOfConnection = gson.fromJson(lines.toString(), connectionsListType);
        listOfConnection = listOfConnection == null ? new ArrayList() : listOfConnection;
    }
    
    private StringBuilder tryReadLinesFromFile(File file) {
        StringBuilder lines = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.append(line);
            }
            return lines;
        } catch (IOException ex) {
            System.err.println("Ошибка чтения из файла");
            return new StringBuilder();
        }
    }
}

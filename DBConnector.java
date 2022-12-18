package com.example.gamepbo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.sql.*;

public class DBConnector {
    private TableView tableView;
    private ObservableList<ObservableList> queryData;

    public DBConnector() {
        queryData = null;
        tableView = null;
    }

    public TableView getTable() { return tableView; }

    public ObservableList<ObservableList> getData() { return queryData; }

    public void start(String dbCommand) {
        try (Connection dbConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gamepbo?serverTimezone=UTC",
                "root", "");
             Statement statement = dbConnection.createStatement()) {

            ResultSet queryResult = statement.executeQuery(dbCommand);

            queryData = FXCollections.observableArrayList();
            tableView = new TableView();

            for (int i = 0; i < queryResult.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(queryResult.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                col.prefWidthProperty().bind(tableView.widthProperty().divide(4.1));
                tableView.getColumns().addAll(col);
            }

            while (queryResult.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= queryResult.getMetaData().getColumnCount(); i++) {
                    row.add(queryResult.getString(i));
                }
                queryData.add(row);
            }
            tableView.setItems(queryData);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void insert(String dbCommand) {
        try (Connection dbConnection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/gamepbo?serverTimezone=UTC",
                "root", "");
             Statement statement = dbConnection.createStatement()) {
            statement.executeUpdate(dbCommand);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

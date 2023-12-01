package controller;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dto.CustomerDto;
import dto.ItemDto;
import dto.OrderDetailsDto;
import dto.OrderDto;
import dto.tm.OrderTm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.CustomerModel;
import model.ItemModel;
import model.OrderModel;
import model.impl.CustomerModelImpl;
import model.impl.ItemModelImpl;
import model.impl.OrderModelImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PlaceOrderFormController {

    public AnchorPane pane;
    @FXML
    private JFXComboBox<?> cmbCustId;

    @FXML
    private JFXComboBox<?> cmbItemCode;

    @FXML
    private JFXTextField txtCustName;

    @FXML
    private JFXTextField txtDesc;

    @FXML
    private JFXTextField txtUnitPrice;

    @FXML
    private JFXTextField txtQty;

    @FXML
    private JFXTreeTableView<OrderTm> tblOrder;

    @FXML
    private TreeTableColumn<?, ?> colCode;

    @FXML
    private TreeTableColumn<?, ?> colDesc;

    @FXML
    private TreeTableColumn<?, ?> colQty;

    @FXML
    private TreeTableColumn<?, ?> colAmount;

    @FXML
    private TreeTableColumn<?, ?> colOption;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblOrderId;


    private List<CustomerDto> customers;
    private List<ItemDto> items;

    private CustomerModel customerModel=new CustomerModelImpl();
    private ItemModel itemModel=new ItemModelImpl();
    private ObservableList<OrderTm>tmList=FXCollections.observableArrayList();
    private OrderModel orderModel =new OrderModelImpl();
    double tot=0;
    @FXML
    void addToCartButtonOnAction(ActionEvent event) {

        try {
            double amount=itemModel.getItem(cmbItemCode.getValue().toString()).getUnitPrice() * Integer.parseInt(txtQty.getText()); ;
            JFXButton btn =new JFXButton("Delete");
            OrderTm tm =new OrderTm(
                    cmbItemCode.getValue().toString(),
                    txtDesc.getText(),
                    Integer.parseInt(txtQty.getText()),
                    amount,
                    btn);
            boolean isExist=false;

            btn.setOnAction(actionEvent -> {
                tmList.remove(tm);
                tblOrder.refresh();
                tot-=tm.getAmount();
                lblTotal.setText(String.format("%.2f",tot));
            });




            for (OrderTm order:tmList){
                if(order.getCode().equals(tm.getCode())) {
                    order.setQty(order.getQty() + tm.getQty());
                    order.setAmount(order.getAmount() + tm.getAmount());
                    isExist = true;
                    tot += tm.getAmount();
                }

            }
            if(!isExist){
                tmList.add(tm);
                tot+=tm.getAmount();

            }
            TreeItem<OrderTm> treeItem = new RecursiveTreeItem<OrderTm>(tmList, RecursiveTreeObject::getChildren);
            tblOrder.setRoot(treeItem);
            tblOrder.setShowRoot(false);

            lblTotal.setText(String.format("%.2f",tot));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        txtQty.clear();

    }

    @FXML
    void backButtonOnAction(ActionEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/DashboardForm.fxml"))));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void generateId(){
        try {
            OrderDto dto = orderModel.lastOrder();
            if (dto!=null){
                String id = dto.getOrderId();
                int num = Integer.parseInt(id.split("[D]")[1]);
                num++;
                lblOrderId.setText(String.format("D%03d",num));
            }else{
                lblOrderId.setText("D001");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void placeOrderButtonOnAction(ActionEvent event) {
        List<OrderDetailsDto>list=new ArrayList<>();

        for (OrderTm tm:tmList){
            list.add(new OrderDetailsDto(
                    lblOrderId.getText(),
                    tm.getCode(),
                    tm.getQty(),
                    tm.getAmount()/ tm.getQty()
            ));
        }
        if(!tmList.isEmpty()){
            boolean isSaved = false;
            try {
                isSaved = orderModel.saveOrder(new OrderDto(
                        lblOrderId.getText(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-DD")).toString(),
                        cmbCustId.getValue().toString(),
                        list

                ));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (isSaved){
                new Alert(Alert.AlertType.INFORMATION,"Order Saved !").show();
            }
        }

    }
    public void initialize(){
        colCode.setCellValueFactory(new TreeItemPropertyValueFactory<>("code"));
        colDesc.setCellValueFactory(new TreeItemPropertyValueFactory<>("desc"));
        colQty.setCellValueFactory(new TreeItemPropertyValueFactory<>("qty"));
        colAmount.setCellValueFactory(new TreeItemPropertyValueFactory<>("amount"));
        colOption.setCellValueFactory(new TreeItemPropertyValueFactory<>("btn"));


        generateId();
        loadCustomerId();
        loadItemCodes();
        txtUnitPrice.setEditable(false);
        txtDesc.setEditable(false);
        txtCustName.setEditable(false);

        cmbCustId.getSelectionModel().selectedItemProperty().addListener(((observableValue,oldValue,id) -> {
            for (CustomerDto dto : customers) {
                if (dto.getId().equals(id)){
                    txtCustName.setText(dto.getName());
                }
            }
        }));
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(((observableValue,oldValue,id) -> {
            for (ItemDto dto : items) {
                if (dto.getCode().equals(id)){
                    txtDesc.setText(dto.getDesc());
                    txtUnitPrice.setText(String.valueOf(dto.getUnitPrice()));
                }
            }
        }));




    }

    private void loadItemCodes() {
        try {
            items=itemModel.allItem();
            ObservableList list= FXCollections.observableArrayList();
            for(ItemDto dto:items){
                list.add(dto.getCode());
            }
            cmbItemCode.setItems(list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void loadCustomerId() {
        try {
            customers=customerModel.allCustomers();
            ObservableList list= FXCollections.observableArrayList();
            for(CustomerDto dto:customers){
                list.add(dto.getId());
            }
            cmbCustId.setItems(list);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}

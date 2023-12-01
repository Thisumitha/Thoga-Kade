package model.impl;

import db.DBConnection;
import dto.OrderDetailsDto;
import model.OrderDetailsModel;
import model.OrderModel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderDetailsModelImpl implements OrderDetailsModel {
    @Override
    public boolean saveOrderDetails(List<OrderDetailsDto> list) throws SQLException, ClassNotFoundException {
        boolean isDetailsSaved=false;
        for (OrderDetailsDto dto :list){
            String sql="INSERT INTO orderdetetails Values (?,?,?,?)";
            PreparedStatement pstm = DBConnection.getInstance().getConnection().prepareStatement(sql);
            pstm.setString(1, dto.getOrderId());
            pstm.setString(2, dto.getItemCode());
            pstm.setInt(3, dto.getQty());
            pstm.setDouble(4,dto.getUnitPrice());
        }
        return false;
    }
}

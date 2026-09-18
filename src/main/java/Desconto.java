import javax.swing.JOptionPane;

public class Desconto {
    public static void main(String[] args){
        String ValorDaCompraStr = JOptionPane.showInputDialog("Qual o valor da compra?");
        double ValorDaCompra = Double.parseDouble(ValorDaCompraStr);
        String ValorDoDescontoStr = JOptionPane.showInputDialog("Qual a valor percentual do deconto?");
        double ValorDesconto = Double.parseDouble(ValorDoDescontoStr);
        double ValorComDesconto = ValorDaCompra - (ValorDaCompra*ValorDesconto/100);
        JOptionPane.showMessageDialog(null,"Você irá pagar " + ValorComDesconto);
    }

}

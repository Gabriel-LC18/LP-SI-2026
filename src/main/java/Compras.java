import javax.swing.JOptionPane;

public class Compras {
    public static void main(String [] args){
        String MacaStr = JOptionPane.showInputDialog("Quantas maçãs compradas");
        int Maca = Integer.parseInt(MacaStr);
        String MamaoStr = JOptionPane.showInputDialog("Quantos mamões comprados?");
        int Mamao = Integer.parseInt(MamaoStr);
        double ValorTotal = (Maca*1.0) + (Mamao*3.50);
        JOptionPane.showMessageDialog(null,"Você pagará"+ ValorTotal);
    }


}

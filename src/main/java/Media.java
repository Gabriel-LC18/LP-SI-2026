import javax.swing.JOptionPane;

public class Media {
    public static void main(String [] args){
        String nota1String = JOptionPane.showInputDialog("Qual a primeira nota?");
        double nota1 = Double.parseDouble(nota1String);
        String nota2String = JOptionPane.showInputDialog("Qual a segunda nota?");
        double nota2 = Double.parseDouble(nota2String);
        double media = (nota1+nota2) / 2;
        JOptionPane.showMessageDialog(null,"Sua média é " + media);
    }
}
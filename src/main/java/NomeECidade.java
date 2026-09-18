import javax.swing.JOptionPane;

public class NomeECidade {
    public static void main(String[] args){
        String Nome = JOptionPane.showInputDialog("Qual o seu nome?");
        String Cidade = JOptionPane.showInputDialog("Qual a cidade que você nasceu?");
        System.out.println("OI "+Nome+"! Que legal saber que você é da cidade "+ Cidade);

    }
}

import javax.swing.JOptionPane;

public class Oi {

    public static void mostreMensagemBoasVindas(String nome){
        System.out.println("Oi "+ nome);
        System.out.println("Como vai você?");
    }


    public static void main(String [] args){
        String nomeLido = JOptionPane.showInputDialog("Diga seu nome");
        mostreMensagemBoasVindas(nomeLido);
        int x = 10;
        String nome = "Gabriel";
        System.out.println(nome.charAt(0));
        System.out.println(nome.charAt(6));


        char letra = 'c';
        double altura = 1.75;
        float numeroReal = 8.87f;
        boolean toComFome = true;
    }

}
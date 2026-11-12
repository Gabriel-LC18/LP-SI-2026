package projeto.Calculo.Lira.Gabriel;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;


/**
 * RALI CARTESIANO - jogo educativo de tabuleiro sobre funções (Cálculo Diferencial e Integral).
 *
 * Compilar:  javac -encoding UTF-8 RaliCartesiano.java
 * Executar:  java RaliCartesiano
 *
 * Todos os carros são desenhados por código (Java2D), então não é preciso nenhuma imagem.
 */
public class RaliCartesiano extends JFrame {

    static final int FINISH = 30;
    static final String MENU = "menu", RULES = "rules", SETUP = "setup", GAME = "game", PODIUM = "podium";
    static final Font F = new Font("SansSerif", Font.PLAIN, 16);

    static RaliCartesiano app;
    final CardLayout cards = new CardLayout();
    final JPanel root = new JPanel(cards);
    SetupPanel setupPanel;
    GamePanel gamePanel;
    PodiumPanel podiumPanel;

    public RaliCartesiano() {
        super("Rali Cartesiano");
        app = this;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setupPanel = new SetupPanel();
        gamePanel = new GamePanel();
        podiumPanel = new PodiumPanel();
        root.add(new MenuPanel(), MENU);
        root.add(new RulesPanel(), RULES);
        root.add(setupPanel, SETUP);
        root.add(gamePanel, GAME);
        root.add(podiumPanel, PODIUM);
        setContentPane(root);
        show(MENU);
    }

    void show(String name) { cards.show(root, name); }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) { }
        SwingUtilities.invokeLater(() -> new RaliCartesiano().setVisible(true));
    }

    // =====================================================================
    //  DADOS: pilotos, jogadores, perguntas e cartas
    // =====================================================================

    static class Piloto {
        final String nome; final Color corpo, detalhe;
        Piloto(String n, Color c, Color d) { nome = n; corpo = c; detalhe = d; }
    }

    static final Piloto[] PILOTOS = {
            new Piloto("Relâmpago McQueen", new Color(215, 25, 32), new Color(255, 200, 0)),
            new Piloto("Sally Carrera", new Color(120, 170, 235), new Color(255, 255, 255)),
            new Piloto("Mate", new Color(165, 115, 70), new Color(95, 65, 40)),
            new Piloto("Doc Hudson", new Color(30, 60, 140), new Color(220, 220, 230)),
            new Piloto("Luigi", new Color(240, 220, 60), new Color(255, 255, 255)),
            new Piloto("Ramone", new Color(150, 60, 170), new Color(255, 140, 0)),
            new Piloto("Fillmore", new Color(80, 170, 90), new Color(255, 255, 255)),
            new Piloto("Sargento", new Color(85, 100, 50), new Color(200, 190, 140)),
            new Piloto("Flo", new Color(40, 170, 170), new Color(255, 215, 230)),
            new Piloto("Chick Hicks", new Color(60, 150, 60), new Color(255, 255, 255)),
            new Piloto("Francesco Bernoulli", new Color(200, 30, 30), new Color(255, 255, 255)),
            new Piloto("Xerife", new Color(25, 25, 30), new Color(240, 240, 240)),
    };

    static class Jogador {
        String nome; Piloto piloto; int pos = 0; double vis = 0; int parado = 0; boolean escudo = false; int rank = 0;
        Jogador(String n, Piloto p) { nome = n; piloto = p; }
    }

    static class Pergunta {
        final String texto, certa, exp; final String[] erradas;
        DoubleUnaryOperator fn; double x0, x1, y0, y1;
        Pergunta(String t, String c, String e1, String e2, String e3, String exp) {
            texto = t; certa = c; erradas = new String[]{e1, e2, e3}; this.exp = exp;
        }
        Pergunta grafico(DoubleUnaryOperator f, double x0, double x1, double y0, double y1) {
            fn = f; this.x0 = x0; this.x1 = x1; this.y0 = y0; this.y1 = y1; return this;
        }
    }

    enum Efeito { AVANCAR, VOLTAR, PARADO, TROCA_LIDER, TROCA_ULTIMO, JOGAR_DE_NOVO, ESCUDO, EMPURRAR }

    static class Carta {
        final String titulo, msg; final Efeito ef; final int n;
        Carta(String t, String m, Efeito e, int n) { titulo = t; msg = m; ef = e; this.n = n; }
    }

    static List<Pergunta> criarPerguntas() {
        List<Pergunta> q = new ArrayList<>();
        q.add(new Pergunta("Dada a função f(x) = 2x + 3, qual é o valor de f(4)?",
                "11", "8", "14", "9",
                "Substituímos x por 4:\nf(4) = 2·4 + 3 = 8 + 3 = 11."));
        q.add(new Pergunta("Qual é o zero (raiz) da função f(x) = 3x − 12?",
                "x = 4", "x = −4", "x = 12", "x = −12",
                "O zero é o valor de x para o qual f(x) = 0.\n3x − 12 = 0  →  3x = 12  →  x = 4."));
        q.add(new Pergunta("Qual é o domínio real da função f(x) = √(x − 2)?",
                "x ≥ 2", "x > 2", "x ≤ 2", "Todos os reais (ℝ)",
                "O radicando de uma raiz quadrada não pode ser negativo: x − 2 ≥ 0  →  x ≥ 2.\n(x = 2 é permitido, pois √0 = 0.)"));
        q.add(new Pergunta("Qual é o domínio da função f(x) = (x + 1) / (x² − 9)?",
                "ℝ − {−3, 3}", "ℝ − {−1}", "ℝ − {9}", "ℝ − {3}",
                "O denominador não pode ser zero: x² − 9 = 0  →  x = 3 ou x = −3.\nPortanto D = ℝ − {−3, 3}."));
        q.add(new Pergunta("Quais são as raízes de f(x) = x² − 4x + 3?",
                "1 e 3", "−1 e −3", "1 e −3", "2 e 3",
                "Bhaskara: Δ = (−4)² − 4·1·3 = 16 − 12 = 4.\nx = (4 ± 2) / 2  →  x = 3 ou x = 1."));
        q.add(new Pergunta("Qual é o vértice da parábola f(x) = x² − 6x + 5?",
                "(3, −4)", "(−3, −4)", "(3, 4)", "(6, 5)",
                "xv = −b / 2a = 6 / 2 = 3.\nyv = f(3) = 9 − 18 + 5 = −4.  Vértice: (3, −4)."));
        q.add(new Pergunta("Qual é o conjunto imagem de f: ℝ → ℝ, f(x) = x²?",
                "[0, +∞)", "ℝ", "(−∞, 0]", "(0, +∞)",
                "Um número real elevado ao quadrado nunca é negativo, e o valor 0 é atingido em x = 0.\nIm(f) = [0, +∞)."));
        q.add(new Pergunta("A função f(x) = −2x + 5 é crescente ou decrescente?",
                "Decrescente, pois a = −2 < 0", "Crescente, pois b = 5 > 0", "Constante, pois b é fixo",
                "Crescente, pois |a| > 1",
                "Em f(x) = ax + b, o sinal de a decide: a > 0 → crescente; a < 0 → decrescente.\nAqui a = −2, então a função é decrescente."));
        q.add(new Pergunta("Sejam f(x) = x + 1 e g(x) = 2x. Quanto vale (f ∘ g)(3)?",
                "7", "8", "9", "6",
                "(f ∘ g)(3) = f(g(3)). Primeiro g(3) = 2·3 = 6; depois f(6) = 6 + 1 = 7.\n(O valor 8 seria (g ∘ f)(3): a ordem importa!)"));
        q.add(new Pergunta("Sejam f(x) = x² e g(x) = x + 3. Quanto vale (g ∘ f)(2)?",
                "7", "25", "10", "5",
                "(g ∘ f)(2) = g(f(2)). Primeiro f(2) = 2² = 4; depois g(4) = 4 + 3 = 7.\n(O valor 25 seria (f ∘ g)(2).)"));
        q.add(new Pergunta("Qual é a função inversa de f(x) = 2x − 6?",
                "f⁻¹(x) = (x + 6) / 2", "f⁻¹(x) = (x − 6) / 2", "f⁻¹(x) = 2x + 6", "f⁻¹(x) = x/2 − 6",
                "Escrevemos y = 2x − 6 e isolamos x: x = (y + 6) / 2.\nTrocando y por x: f⁻¹(x) = (x + 6) / 2."));
        q.add(new Pergunta("Se f(x) = |x − 3|, qual é o valor de f(−1)?",
                "4", "−4", "2", "3",
                "f(−1) = |−1 − 3| = |−4| = 4. O módulo nunca é negativo."));
        q.add(new Pergunta("Considere a relação de A = {1, 2, 3} em B = {a, b, c}:\n{(1,a), (2,b), (2,c), (3,a)}.\nEla é uma função de A em B?",
                "Não, pois o elemento 2 tem duas imagens", "Sim, pois todo elemento de A tem correspondente",
                "Não, pois o elemento a aparece duas vezes", "Sim, pois B tem 3 elementos",
                "Para ser função, cada elemento de A deve ter UMA única imagem.\nO elemento 2 está ligado a b e a c, então não é função. (Repetir elementos de B, como o a, é permitido.)"));
        q.add(new Pergunta("O gráfico é de uma função afim. Qual é o zero dessa função?",
                "x = 2", "x = −2", "x = 0", "x = 1",
                "O zero é o ponto onde o gráfico cruza o eixo x. A reta é f(x) = x − 2:\nx − 2 = 0  →  x = 2.").grafico(x -> x - 2, -4, 6, -5, 5));
        q.add(new Pergunta("Quais são as raízes da função cujo gráfico está ao lado?",
                "−2 e 2", "0 e 4", "−4 e 4", "Apenas 4",
                "O gráfico é de f(x) = −x² + 4. As raízes são onde a parábola corta o eixo x:\n−x² + 4 = 0  →  x² = 4  →  x = ±2.").grafico(x -> -x * x + 4, -4, 4, -3, 6));
        q.add(new Pergunta("O gráfico é uma transformação de y = x². Qual é a lei da função?",
                "f(x) = (x − 1)²", "f(x) = (x + 1)²", "f(x) = x² − 1", "f(x) = x² + 1",
                "O vértice passou de (0, 0) para (1, 0): a parábola foi deslocada 1 unidade para a DIREITA.\nIsso corresponde a f(x) = (x − 1)².").grafico(x -> (x - 1) * (x - 1), -3, 5, -1, 9));
        q.add(new Pergunta("Qual valor de x NÃO pertence ao domínio da função do gráfico?",
                "x = 2", "x = 0", "x = −2", "x = 1",
                "O gráfico é de f(x) = 1 / (x − 2). Em x = 2 o denominador vira zero e a função não existe.\nD = ℝ − {2}.").grafico(x -> 1 / (x - 2), -4, 8, -6, 6));
        q.add(new Pergunta("Qual é o conjunto imagem da função do gráfico?",
                "[−2, +∞)", "[0, +∞)", "ℝ", "(−∞, −2]",
                "O gráfico é de f(x) = |x| − 2. O ponto mais baixo é y = −2 e a curva sobe indefinidamente.\nIm(f) = [−2, +∞).").grafico(x -> Math.abs(x) - 2, -5, 5, -4, 5));
        q.add(new Pergunta("Em qual intervalo a função do gráfico é decrescente?",
                "x < 2", "x > 2", "x < −1", "−1 < x < 3",
                "A parábola f(x) = x² − 4x + 3 tem vértice em x = 2.\nÀ esquerda do vértice o gráfico desce (decrescente): x < 2.").grafico(x -> x * x - 4 * x + 3, -1, 5, -2, 6));
        q.add(new Pergunta("Observe a tabela:\n   x:     0   1   2   3\n f(x):   1   3   5   7\nQual é a lei da função afim?",
                "f(x) = 2x + 1", "f(x) = x + 1", "f(x) = 3x", "f(x) = 2x − 1",
                "f(0) = 1  →  b = 1.\nA cada aumento de 1 em x, f(x) aumenta 2  →  a = 2.  Logo f(x) = 2x + 1."));
        q.add(new Pergunta("Seja f: {1, 2, 3} → {a, b, c, d} com f(1) = a, f(2) = a e f(3) = c.\nQual é o conjunto imagem de f?",
                "{a, c}", "{a, b, c, d}", "{1, 2, 3}", "{a}",
                "O contradomínio é {a, b, c, d}, mas a imagem só tem os elementos realmente atingidos: a e c.\nIm(f) = {a, c}."));
        q.add(new Pergunta("Se f(x) = x² − 3x, quanto vale f(−2)?",
                "10", "−10", "−2", "2",
                "f(−2) = (−2)² − 3·(−2) = 4 + 6 = 10."));
        q.add(new Pergunta("Uma função afim passa pelos pontos (0, 3) e (1, 5). Quanto vale f(10)?",
                "23", "20", "25", "13",
                "Em x = 0: b = 3. De x = 0 para x = 1, y sobe 2, então a = 2.\nf(x) = 2x + 3  →  f(10) = 23."));
        q.add(new Pergunta("Quantas raízes reais tem f(x) = x² + 2x + 5?",
                "Nenhuma", "Uma", "Duas", "Três",
                "Δ = 2² − 4·1·5 = 4 − 20 = −16 < 0.\nCom Δ negativo não há raízes reais (a parábola não toca o eixo x)."));
        q.add(new Pergunta("Se f(x) = x + 5, qual é o valor de f⁻¹(9)?",
                "4", "14", "−4", "9",
                "A inversa é f⁻¹(x) = x − 5. Logo f⁻¹(9) = 9 − 5 = 4.\n(Confira: f(4) = 9.)"));
        q.add(new Pergunta("Em qual intervalo f(x) = x² é decrescente?",
                "(−∞, 0]", "[0, +∞)", "Em todo ℝ", "Em nenhum intervalo",
                "A parábola tem vértice em (0, 0). À esquerda do vértice, quando x aumenta, f(x) diminui.\nPortanto é decrescente em (−∞, 0]."));
        q.add(new Pergunta("Se g(x) = f(x) + 3, o gráfico de g em relação ao de f é:",
                "Deslocado 3 unidades para cima", "Deslocado 3 unidades para baixo",
                "Deslocado 3 unidades para a direita", "Deslocado 3 unidades para a esquerda",
                "Somar uma constante DEPOIS de calcular f aumenta todos os valores de y em 3:\no gráfico sobe 3 unidades."));
        q.add(new Pergunta("Quais são as soluções de |2x − 1| = 5?",
                "x = 3 ou x = −2", "x = 3 ou x = 2", "x = −3 ou x = 2", "Apenas x = 3",
                "2x − 1 = 5  →  x = 3.\n2x − 1 = −5  →  x = −2.\nO módulo igual a 5 permite os dois casos."));
        q.add(new Pergunta("Qual é o zero da função f(x) = −x/2 + 3?",
                "x = 6", "x = −6", "x = 3", "x = 1,5",
                "−x/2 + 3 = 0  →  x/2 = 3  →  x = 6."));
        q.add(new Pergunta("Qual das leis abaixo representa uma função quadrática?",
                "f(x) = 3x² − x", "f(x) = 3x − 1", "f(x) = |x|", "f(x) = 1 / x",
                "Função quadrática tem a forma ax² + bx + c com a ≠ 0.\nf(x) = 3x² − x tem a = 3, b = −1, c = 0."));
        return q;
    }

    static List<Carta> cartasBoas() {
        List<Carta> c = new ArrayList<>();
        c.add(new Carta("Turbo Ka-chow!", "Você acelerou como um campeão da Copa Pistão! Avance 3 casas.", Efeito.AVANCAR, 3));
        c.add(new Carta("Vento a favor", "Um ventinho do deserto empurrou seu carro. Avance 2 casas.", Efeito.AVANCAR, 2));
        c.add(new Carta("Nitro Cartesiano", "Eixo x, lá vou eu! Avance 4 casas.", Efeito.AVANCAR, 4));
        c.add(new Carta("Reboque do Mate", "\"Dá-lhe, dá-lhe! Dirijo de ré, mas rebocando pra frente!\" Avance 5 casas.", Efeito.AVANCAR, 5));
        c.add(new Carta("Dica do Doc Hudson", "\"Vire à direita para ir à esquerda.\" Você pegou a curva perfeita: avance 3 casas.", Efeito.AVANCAR, 3));
        c.add(new Carta("Vácuo do líder", "Você pegou o vácuo e trocou de lugar com o líder da corrida! (Se você já é o líder, avance 2 casas.)", Efeito.TROCA_LIDER, 0));
        c.add(new Carta("Mais uma volta!", "Está pegando fogo, bicho! Jogue novamente.", Efeito.JOGAR_DE_NOVO, 0));
        c.add(new Carta("Cera da Flo", "Seu carro ficou brilhando e protegido! Você ignora a PRÓXIMA carta negativa.", Efeito.ESCUDO, 0));
        c.add(new Carta("Pegadinha do Mate", "Escolha um adversário: ele volta 2 casas. Hi-hi-hi!", Efeito.EMPURRAR, 2));
        c.add(new Carta("Pit stop do Luigi", "Pneus novinhos, \"Pneus Zonda\"! Avance 2 casas.", Efeito.AVANCAR, 2));
        return c;
    }

    static List<Carta> cartasRuins() {
        List<Carta> c = new ArrayList<>();
        c.add(new Carta("Pneu furado", "Opa! Você passou por uns cacos de vidro. Volte 2 casas.", Efeito.VOLTAR, 2));
        c.add(new Carta("Areia na pista", "Você derrapou no deserto. Volte 1 casa.", Efeito.VOLTAR, 1));
        c.add(new Carta("Óleo na pista", "Ihhh, escorregou feio! Volte 3 casas.", Efeito.VOLTAR, 3));
        c.add(new Carta("Pit stop demorado", "Os mecânicos estão trocando seus pneus... bem devagar. Fique 1 rodada sem jogar.", Efeito.PARADO, 1));
        c.add(new Carta("Sem gasolina", "O tanque secou! Fique 1 rodada sem jogar.", Efeito.PARADO, 1));
        c.add(new Carta("Radar do Xerife", "Você estava acima da velocidade! Fique 1 rodada sem jogar.", Efeito.PARADO, 1));
        c.add(new Carta("Chick Hicks te fechou", "Que trapaceiro! Ele te fechou na curva. Volte 3 casas.", Efeito.VOLTAR, 3));
        c.add(new Carta("Tratores assustados", "Você acordou os tratores do Mate. Que barulheira! Volte 2 casas.", Efeito.VOLTAR, 2));
        c.add(new Carta("Troca forçada", "Você troca de lugar com o ÚLTIMO colocado. (Se você já é o último, volte 1 casa.)", Efeito.TROCA_ULTIMO, 0));
        c.add(new Carta("Poeira nos olhos", "Você não enxergou a curva. Volte 2 casas.", Efeito.VOLTAR, 2));
        return c;
    }

    static class Baralho<T> {
        final List<T> origem; final List<T> fila = new ArrayList<>();
        Baralho(List<T> o) { origem = o; }
        T proxima() {
            if (fila.isEmpty()) { fila.addAll(origem); Collections.shuffle(fila); }
            return fila.remove(fila.size() - 1);
        }
    }

    // =====================================================================
    //  DESENHO: carros, troféu, cenário
    // =====================================================================

    static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Desenha um carro estilo "Carros" (olhos no para-brisa). mood: 0 = feliz, 1 = chorando. */
    static void desenharCarro(Graphics2D g0, double cx, double cy, double s, Piloto p, boolean esquerda, int mood) {
        Graphics2D g = (Graphics2D) g0.create();
        aa(g);
        g.translate(cx, cy);
        g.scale(esquerda ? -s : s, s);
        // sombra
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(-30, 15, 60, 9));
        // corpo
        g.setColor(p.corpo);
        g.fill(new RoundRectangle2D.Double(-30, -8, 60, 23, 18, 18));
        g.fill(new RoundRectangle2D.Double(-15, -21, 32, 18, 14, 14));
        // faixa
        g.setColor(p.detalhe);
        g.fill(new RoundRectangle2D.Double(-28, 5, 56, 4, 4, 4));
        // para-brisa
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(-12, -19, 28, 14, 10, 10));
        g.setColor(new Color(0, 0, 0, 120));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(-12, -19, 28, 14, 10, 10));
        // olhos
        g.setColor(new Color(60, 120, 200));
        g.fill(new Ellipse2D.Double(-10, -17.5, 11, 10.5));
        g.fill(new Ellipse2D.Double(3, -17.5, 11, 10.5));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(-6, -15, 5, 5));
        g.fill(new Ellipse2D.Double(7, -15, 5, 5));
        // boca
        g.setColor(new Color(40, 20, 20));
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (mood == 1) g.draw(new Arc2D.Double(8, 0.5, 14, 8, 0, 180, Arc2D.OPEN));
        else g.draw(new Arc2D.Double(8, -3, 14, 8, 180, 180, Arc2D.OPEN));
        // lágrimas
        if (mood == 1) {
            double t = (System.currentTimeMillis() % 900) / 900.0;
            g.setColor(new Color(90, 170, 255, 220));
            for (int i = 0; i < 2; i++) {
                double ex = i == 0 ? -4 : 9.5;
                double yy = -7 + t * 16;
                g.fill(new Ellipse2D.Double(ex - 1.5, yy, 3.5, 5));
            }
        }
        // faróis
        g.setColor(new Color(255, 245, 150));
        g.fill(new Ellipse2D.Double(25, -2, 5, 7));
        g.setColor(new Color(230, 40, 40));
        g.fill(new Ellipse2D.Double(-30, -1, 4, 6));
        // rodas
        for (int x : new int[]{-18, 18}) {
            g.setColor(new Color(25, 25, 30));
            g.fill(new Ellipse2D.Double(x - 7.5, 8, 15, 15));
            g.setColor(new Color(190, 190, 200));
            g.fill(new Ellipse2D.Double(x - 3.5, 12, 7, 7));
        }
        g.dispose();
    }

    static void desenharTrofeu(Graphics2D g0, double x, double y, double s) {
        Graphics2D g = (Graphics2D) g0.create();
        aa(g);
        Color ouro = new Color(255, 200, 40), escuro = new Color(200, 140, 10);
        g.setColor(escuro);
        g.fill(new RoundRectangle2D.Double(x - 9 * s, y - 4 * s, 18 * s, 4 * s, 3 * s, 3 * s));
        g.setColor(ouro);
        g.fill(new Rectangle2D.Double(x - 2 * s, y - 12 * s, 4 * s, 8 * s));
        Path2D cup = new Path2D.Double();
        cup.moveTo(x - 11 * s, y - 34 * s);
        cup.lineTo(x + 11 * s, y - 34 * s);
        cup.curveTo(x + 11 * s, y - 18 * s, x + 4 * s, y - 12 * s, x, y - 12 * s);
        cup.curveTo(x - 4 * s, y - 12 * s, x - 11 * s, y - 18 * s, x - 11 * s, y - 34 * s);
        cup.closePath();
        g.fill(cup);
        g.setStroke(new BasicStroke((float) (2.6 * s)));
        g.setColor(escuro);
        g.draw(new Arc2D.Double(x - 19 * s, y - 33 * s, 13 * s, 13 * s, 90, 180, Arc2D.OPEN));
        g.draw(new Arc2D.Double(x + 6 * s, y - 33 * s, 13 * s, 13 * s, 270, 180, Arc2D.OPEN));
        g.setColor(new Color(255, 255, 255, 170));
        g.setStroke(new BasicStroke((float) (2 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x - 7 * s, y - 30 * s, x - 6 * s, y - 22 * s));
        g.dispose();
    }

    static void ceuDeserto(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, new Color(70, 150, 230), 0, h * 0.7f, new Color(255, 200, 140)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 245, 190));
        g.fill(new Ellipse2D.Double(w * 0.82, h * 0.08, 90, 90));
        g.setColor(new Color(255, 255, 255, 200));
        for (int i = 0; i < 4; i++) {
            double x = (i * 331 + 120) % w, y = 50 + (i * 47) % 120;
            g.fill(new Ellipse2D.Double(x, y, 120, 36));
            g.fill(new Ellipse2D.Double(x + 30, y - 16, 80, 40));
        }
        g.setColor(new Color(190, 100, 60));
        mesa(g, w * 0.05, h * 0.78, w * 0.22, h * 0.28);
        mesa(g, w * 0.28, h * 0.78, w * 0.16, h * 0.18);
        mesa(g, w * 0.62, h * 0.78, w * 0.25, h * 0.32);
        g.setColor(new Color(170, 85, 50));
        mesa(g, w * 0.78, h * 0.78, w * 0.18, h * 0.2);
    }

    static void mesa(Graphics2D g, double x, double yBase, double w, double h) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, yBase);
        p.lineTo(x + w * 0.08, yBase - h * 0.85);
        p.lineTo(x + w * 0.2, yBase - h);
        p.lineTo(x + w * 0.8, yBase - h);
        p.lineTo(x + w * 0.92, yBase - h * 0.8);
        p.lineTo(x + w, yBase);
        p.closePath();
        g.fill(p);
    }

    static void cacto(Graphics2D g, double x, double y, double s) {
        g.setColor(new Color(50, 140, 70));
        g.fill(new RoundRectangle2D.Double(x - 6 * s, y - 48 * s, 12 * s, 48 * s, 10 * s, 10 * s));
        g.fill(new RoundRectangle2D.Double(x - 20 * s, y - 34 * s, 8 * s, 20 * s, 8 * s, 8 * s));
        g.fill(new RoundRectangle2D.Double(x - 20 * s, y - 20 * s, 18 * s, 7 * s, 7 * s, 7 * s));
        g.fill(new RoundRectangle2D.Double(x + 12 * s, y - 40 * s, 8 * s, 20 * s, 8 * s, 8 * s));
        g.fill(new RoundRectangle2D.Double(x + 2 * s, y - 26 * s, 18 * s, 7 * s, 7 * s, 7 * s));
    }

    static class IconeCarro implements Icon {
        final Piloto p; final int w, h;
        IconeCarro(Piloto p, int w, int h) { this.p = p; this.w = w; this.h = h; }
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            desenharCarro((Graphics2D) g, x + w / 2.0, y + h / 2.0 - 1, Math.min(w / 66.0, h / 46.0), p, false, 0);
        }
    }

    static String html(String s) {
        return "<html><div style='width:230px;text-align:center'>" + s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>";
    }
    static JTextArea areaTexto(String s, Font f, int largura) {
        JTextArea a = new JTextArea(s);
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setFocusable(false); a.setOpaque(false); a.setFont(f);
        a.setSize(largura, 10);
        a.setPreferredSize(new Dimension(largura, a.getPreferredSize().height));
        return a;
    }
    static class Som {
        static final AudioFormat FMT = new AudioFormat(22050f, 16, 1, true, false);
        static boolean ligado = true;

        /** Cada segmento: {freqInicial, freqFinal, duracao(s), volume, ruido(0..1)} */
        static byte[] sintetizar(double[]... segs) {
            int total = 0;
            for (double[] s : segs) total += (int) (s[2] * 22050);
            byte[] out = new byte[total * 2];
            Random r = new Random(1);
            double fase = 0;
            int i = 0;
            for (double[] s : segs) {
                int n = (int) (s[2] * 22050);
                for (int k = 0; k < n; k++) {
                    double t = k / (double) n;
                    fase += 2 * Math.PI * (s[0] + (s[1] - s[0]) * t) / 22050.0;
                    double onda = Math.signum(Math.sin(fase)) * 0.5 + Math.sin(fase) * 0.5;
                    double v = onda * (1 - s[4]) + (r.nextDouble() * 2 - 1) * s[4];
                    double env = Math.min(1, Math.min(k / (22050 * 0.005), (n - k) / (22050 * 0.03)));
                    short x = (short) (v * env * s[3] * 32000);
                    out[2 * i] = (byte) (x & 0xff);
                    out[2 * i + 1] = (byte) ((x >> 8) & 0xff);
                    i++;
                }
            }
            return out;
        }

        static void tocar(byte[] dados) {
            if (!ligado) return;
            new Thread(() -> {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(FMT, dados, 0, dados.length);
                    clip.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.STOP) clip.close(); });
                    clip.start();
                } catch (Exception e) { /* sem placa de som: ignora */ }
            }).start();
        }

        static boolean arquivo(String nome) {
            File f = new File("sons", nome);
            if (!ligado || !f.exists()) return false;
            new Thread(() -> {
                try (AudioInputStream in = AudioSystem.getAudioInputStream(f)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(in);
                    clip.addLineListener(ev -> { if (ev.getType() == LineEvent.Type.STOP) clip.close(); });
                    clip.start();
                } catch (Exception e) { }
            }).start();
            return true;
        }

        static void clique() { tocar(sintetizar(new double[]{880, 880, 0.04, 0.3, 0})); }

        static void acerto() {
            tocar(sintetizar(new double[]{523, 523, 0.09, 0.5, 0}, new double[]{659, 659, 0.09, 0.5, 0},
                    new double[]{784, 784, 0.09, 0.5, 0}, new double[]{1047, 1047, 0.25, 0.5, 0}));
        }

        static void erro() {
            tocar(sintetizar(new double[]{300, 220, 0.2, 0.5, 0.1}, new double[]{220, 110, 0.35, 0.5, 0.1}));
        }

        static void motor() {
            tocar(sintetizar(new double[]{70, 260, 0.35, 0.5, 0.35}, new double[]{260, 320, 0.25, 0.4, 0.35}));
        }

        static void derrapa() { tocar(sintetizar(new double[]{1400, 500, 0.5, 0.35, 0.8})); }

        static void kachow() {
            if (arquivo("kachow.wav")) return;
            tocar(sintetizar(new double[]{900, 900, 0.04, 0.6, 0.9}, new double[]{0, 0, 0.06, 0, 0},
                    new double[]{2200, 400, 0.18, 0.5, 0.1}, new double[]{400, 120, 0.25, 0.5, 0.5}));
        }

        static void vitoria() {
            tocar(sintetizar(new double[]{523, 523, 0.12, 0.5, 0}, new double[]{523, 523, 0.12, 0.5, 0},
                    new double[]{523, 523, 0.12, 0.5, 0}, new double[]{659, 659, 0.3, 0.5, 0},
                    new double[]{587, 587, 0.12, 0.5, 0}, new double[]{784, 784, 0.5, 0.5, 0}));
        }
    }

    // =====================================================================
    //  COMPONENTES DE INTERFACE
    // =====================================================================

    static class BotaoRedondo extends JButton {
        final Color base;
        BotaoRedondo(String t, Color c) {
            super(t);
            base = c;
            setFont(new Font("SansSerif", Font.BOLD, 22));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(320, 60));
            addActionListener(e -> Som.clique());
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            Color c = !isEnabled() ? new Color(150, 150, 150)
                    : getModel().isPressed() ? base.darker()
                    : getModel().isRollover() ? base.brighter() : base;
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(2, 5, getWidth() - 4, getHeight() - 6, 30, 30);
            g.setColor(c);
            g.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 30, 30);
            g.setColor(new Color(255, 255, 255, 110));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(1, 1, getWidth() - 6, getHeight() - 8, 28, 28);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Janela de aviso com "cara de carta". */
    static class CartaDialog extends JDialog {
        CartaDialog(Window dono, String faixa, Color cor, String titulo, String corpo, Piloto carro) {
            super(dono, "Rali Cartesiano", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            JPanel topo = new JPanel(new BorderLayout(12, 0));
            topo.setBackground(cor);
            topo.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            JLabel f = new JLabel(faixa);
            f.setFont(new Font("SansSerif", Font.BOLD, 24));
            f.setForeground(Color.WHITE);
            if (carro != null) topo.add(new JLabel(new IconeCarro(carro, 72, 46)), BorderLayout.WEST);
            topo.add(f, BorderLayout.CENTER);

            JPanel meio = new JPanel();
            meio.setLayout(new BoxLayout(meio, BoxLayout.Y_AXIS));
            meio.setBackground(new Color(255, 250, 235));
            meio.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));
            JLabel t = new JLabel(titulo);
            t.setFont(new Font("SansSerif", Font.BOLD, 22));
            t.setAlignmentX(0f);
            JTextArea a = areaTexto(corpo, new Font("SansSerif", Font.PLAIN, 17), 470);
            a.setAlignmentX(0f);
            meio.add(t);
            meio.add(Box.createVerticalStrut(10));
            meio.add(a);

            BotaoRedondo ok = new BotaoRedondo("OK", cor.darker());
            ok.setPreferredSize(new Dimension(150, 50));
            ok.addActionListener(e -> dispose());
            JPanel sul = new JPanel();
            sul.setBackground(new Color(255, 250, 235));
            sul.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            sul.add(ok);

            setLayout(new BorderLayout());
            add(topo, BorderLayout.NORTH);
            add(meio, BorderLayout.CENTER);
            add(sul, BorderLayout.SOUTH);
            pack();
            setMinimumSize(new Dimension(520, 0));
            setLocationRelativeTo(dono);
        }
        static void mostrar(Component pai, String faixa, Color cor, String titulo, String corpo, Piloto carro) {
            new CartaDialog(SwingUtilities.getWindowAncestor(pai), faixa, cor, titulo, corpo, carro).setVisible(true);
        }
    }

    /** Painel que desenha o gráfico de uma função. */
    static class GraficoPanel extends JPanel {
        final Pergunta q;
        GraficoPanel(Pergunta q) {
            this.q = q;
            setPreferredSize(new Dimension(440, 250));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120), 2));
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            double sx = w / (q.x1 - q.x0), sy = h / (q.y1 - q.y0);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            // grade e marcações
            for (int x = (int) Math.ceil(q.x0); x <= q.x1; x++) {
                int px = (int) ((x - q.x0) * sx);
                g.setColor(x == 0 ? Color.DARK_GRAY : new Color(225, 225, 225));
                g.drawLine(px, 0, px, h);
                if (x != 0) { g.setColor(Color.GRAY); g.drawString("" + x, px + 2, (int) ((q.y1) * sy) + 12 > h - 2 ? h - 3 : clamp((int) (q.y1 * sy) + 12, 12, h - 3)); }
            }
            for (int y = (int) Math.ceil(q.y0); y <= q.y1; y++) {
                int py = (int) ((q.y1 - y) * sy);
                g.setColor(y == 0 ? Color.DARK_GRAY : new Color(225, 225, 225));
                g.drawLine(0, py, w, py);
                if (y != 0) { g.setColor(Color.GRAY); g.drawString("" + y, clamp((int) (-q.x0 * sx) + 3, 2, w - 16), py - 2); }
            }
            g.setColor(Color.DARK_GRAY);
            g.drawString("x", w - 12, clamp((int) (q.y1 * sy) - 4, 12, h - 4));
            g.drawString("y", clamp((int) (-q.x0 * sx) + 5, 2, w - 10), 12);
            // curva
            g.setClip(0, 0, w, h);
            g.setColor(new Color(215, 25, 32));
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Double();
            boolean down = false;
            double span = q.y1 - q.y0, step = (q.x1 - q.x0) / 600.0;
            for (double x = q.x0; x <= q.x1 + 1e-9; x += step) {
                double y = q.fn.applyAsDouble(x);
                if (Double.isNaN(y) || Double.isInfinite(y) || y < q.y0 - span || y > q.y1 + span) { down = false; continue; }
                double px = (x - q.x0) * sx, py = (q.y1 - y) * sy;
                if (!down) { path.moveTo(px, py); down = true; } else path.lineTo(px, py);
            }
            g.draw(path);
            g.dispose();
        }
        static int clamp(int v, int a, int b) { return Math.max(a, Math.min(b, v)); }
    }

    static class PerguntaDialog extends JDialog {
        boolean acertou = false;
        PerguntaDialog(Window dono, Pergunta q, Jogador j) {
            super(dono, "Desafio da função", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            JPanel topo = new JPanel(new BorderLayout(12, 0));
            topo.setBackground(new Color(30, 90, 190));
            topo.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            JLabel l = new JLabel("Desafio para " + j.nome + "  (" + j.piloto.nome + ")");
            l.setFont(new Font("SansSerif", Font.BOLD, 22));
            l.setForeground(Color.WHITE);
            topo.add(new JLabel(new IconeCarro(j.piloto, 72, 46)), BorderLayout.WEST);
            topo.add(l, BorderLayout.CENTER);

            JPanel meio = new JPanel();
            meio.setLayout(new BoxLayout(meio, BoxLayout.Y_AXIS));
            meio.setBackground(new Color(255, 250, 235));
            meio.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
            JTextArea t = areaTexto(q.texto, new Font("SansSerif", Font.BOLD, 20), 510);
            t.setAlignmentX(0.5f);
            meio.add(t);
            if (q.fn != null) {
                meio.add(Box.createVerticalStrut(10));
                GraficoPanel gp = new GraficoPanel(q);
                gp.setAlignmentX(0.5f);
                gp.setMaximumSize(gp.getPreferredSize());
                meio.add(gp);
            }
            meio.add(Box.createVerticalStrut(12));

            List<String> ops = new ArrayList<>(Arrays.asList(q.erradas));
            ops.add(q.certa);
            Collections.shuffle(ops);
            JPanel grade = new JPanel(new GridLayout(2, 2, 10, 10));
            grade.setOpaque(false);
            Color[] cores = {new Color(235, 110, 40), new Color(60, 150, 90), new Color(140, 80, 190), new Color(40, 130, 200)};
            for (int i = 0; i < ops.size(); i++) {
                String op = ops.get(i);
                BotaoRedondo b = new BotaoRedondo(html(op), cores[i]);
                b.setFont(new Font("SansSerif", Font.BOLD, 18));
                b.setPreferredSize(new Dimension(250, 64));
                b.addActionListener(e -> { acertou = op.equals(q.certa); dispose(); });
                grade.add(b);
            }
            grade.setAlignmentX(0.5f);
            meio.add(grade);

            setLayout(new BorderLayout());
            add(topo, BorderLayout.NORTH);
            add(meio, BorderLayout.CENTER);
            pack();
            setLocationRelativeTo(dono);
        }
    }

    // =====================================================================
    //  TELA INICIAL
    // =====================================================================

    static class MenuPanel extends JPanel {
        long t0 = System.currentTimeMillis();
        MenuPanel() {
            setLayout(new GridBagLayout());
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            col.add(botao("INICIAR", new Color(220, 40, 40), () -> app.show(SETUP)));
            col.add(Box.createVerticalStrut(16));
            col.add(botao("REGRAS DO JOGO", new Color(30, 120, 200), () -> app.show(RULES)));
            col.add(Box.createVerticalStrut(16));
            col.add(botao("SAIR", new Color(90, 90, 100), () -> System.exit(0)));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(240, 0, 90, 0);
            add(col, c);
            new Timer(30, e -> { if (isShowing()) repaint(); }).start();
        }
        static JButton botao(String t, Color c, Runnable r) {
            BotaoRedondo b = new BotaoRedondo(t, c);
            b.setAlignmentX(0.5f);
            b.addActionListener(e -> r.run());
            return b;
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            ceuDeserto(g, w, h);
            double tempo = (System.currentTimeMillis() - t0) / 1000.0;
            // eixos cartesianos de fundo
            g.setColor(new Color(255, 255, 255, 70));
            g.setStroke(new BasicStroke(3f));
            g.drawLine(60, 190, w - 60, 190);
            g.drawLine(w / 2, 40, w / 2, 330);
            Path2D par = new Path2D.Double();
            for (int x = -240; x <= 240; x += 6) {
                double px = w / 2.0 + x, py = 330 - (x * x) / 400.0 - 30;
                if (x == -240) par.moveTo(px, py); else par.lineTo(px, py);
            }
            g.draw(par);
            // título
            g.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 92));
            texto(g, "RALI", w / 2, 120, new Color(255, 215, 0));
            g.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 84));
            texto(g, "CARTESIANO", w / 2, 205, new Color(230, 40, 40));
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            texto(g, "f(x) = a corrida mais divertida da matemática!", w / 2, 250, Color.WHITE);
            // estrada
            int roadY = h - 120;
            g.setColor(new Color(80, 80, 90));
            g.fillRect(0, roadY, w, 120);
            g.setColor(new Color(240, 200, 40));
            g.fillRect(0, roadY, w, 5);
            g.setColor(Color.WHITE);
            double off = (tempo * 160) % 80;
            for (double x = -80 + off; x < w; x += 80) g.fillRect((int) x, roadY + 62, 44, 6);
            // carros passando
            for (int i = 0; i < 5; i++) {
                Piloto p = PILOTOS[(i * 2) % PILOTOS.length];
                double vel = 110 + i * 35;
                double x = ((tempo * vel + i * 340) % (w + 200)) - 100;
                int lane = i % 2;
                desenharCarro(g, x, roadY + 30 + lane * 48, 1.15, p, false, 0);
            }
            g.dispose();
        }
        static void texto(Graphics2D g, String s, int cx, int y, Color c) {
            FontMetrics fm = g.getFontMetrics();
            int x = cx - fm.stringWidth(s) / 2;
            g.setColor(new Color(0, 0, 0, 120));
            g.drawString(s, x + 4, y + 4);
            g.setColor(Color.BLACK);
            for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) g.drawString(s, x + dx, y + dy);
            g.setColor(c);
            g.drawString(s, x, y);
        }
    }

    // =====================================================================
    //  TELA DE REGRAS
    // =====================================================================

    static class RulesPanel extends JPanel {
        RulesPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(255, 238, 200));
            JLabel t = new JLabel("Regras do Rali Cartesiano", SwingConstants.CENTER);
            t.setFont(new Font("SansSerif", Font.BOLD, 36));
            t.setForeground(new Color(200, 30, 30));
            t.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            add(t, BorderLayout.NORTH);
            String h = "<html><body style='font-family:sans-serif;font-size:15px;margin:10px 40px'>"
                    + "<p><b>Objetivo:</b> ser um dos primeiros a cruzar a linha de chegada (casa 30). O jogo termina quando só restar um corredor na pista: os demais recebem o pódio na ordem em que chegaram.</p>"
                    + "<p><b>Jogadores:</b> de 2 a 5, cada um com um personagem do filme Carros.</p>"
                    + "<p><b>Na sua vez:</b></p><ol>"
                    + "<li>Clique em <b>PUXAR CARTA DE DESAFIO</b>. Aparece uma questão sobre funções (conta, tabela ou gráfico) com 4 alternativas.</li>"
                    + "<li><b>Acertou?</b> Você puxa uma carta do monte <font color='green'><b>POSITIVO</b></font>: avançar casas, trocar de lugar com o líder, jogar de novo, ganhar um escudo, atrapalhar um adversário...</li>"
                    + "<li><b>Errou?</b> O jogo mostra a resolução passo a passo e você puxa uma carta do monte <font color='red'><b>NEGATIVO</b></font>: voltar casas, ficar uma rodada sem jogar, trocar de lugar com o último...</li>"
                    + "<li>A vez passa para o próximo jogador.</li></ol>"
                    + "<p><b>Escudo:</b> quem tem escudo ignora a próxima carta negativa que puxar.</p>"
                    + "<p><b>Rodada parada:</b> o jogador perde a próxima vez, e depois volta a jogar normalmente.</p>"
                    + "<p><b>Conteúdos de matemática:</b> conceito de função, domínio, contradomínio e imagem, zeros (raízes), função afim e quadrática, "
                    + "módulo, crescimento e decrescimento, leitura de tabelas e gráficos, composição, inversa e transformações de gráficos.</p>"
                    + "<p><b>Dica:</b> papel e lápis são permitidos! Cada resposta errada vem com explicação, então todo mundo aprende.</p>"
                    + "</body></html>";
            JEditorPane ep = new JEditorPane("text/html", h);
            ep.setEditable(false);
            ep.setBackground(new Color(255, 250, 235));
            JScrollPane sp = new JScrollPane(ep);
            sp.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 60));
            sp.getViewport().setBackground(new Color(255, 238, 200));
            add(sp, BorderLayout.CENTER);
            JPanel sul = new JPanel();
            sul.setOpaque(false);
            sul.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
            sul.add(MenuPanel.botao("VOLTAR", new Color(30, 120, 200), () -> app.show(MENU)));
            add(sul, BorderLayout.SOUTH);
        }
    }

    // =====================================================================
    //  TELA DE JOGADORES
    // =====================================================================

    static class SetupPanel extends JPanel {
        static class Slot { JTextField nome = new JTextField(); int piloto; }
        final List<Slot> slots = new ArrayList<>();
        final JPanel lista = new JPanel();
        final BotaoRedondo btnAdd = new BotaoRedondo("+ Adicionar jogador", new Color(60, 150, 90));

        SetupPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(255, 238, 200));
            JLabel t = new JLabel("Quem vai correr?", SwingConstants.CENTER);
            t.setFont(new Font("SansSerif", Font.BOLD, 38));
            t.setForeground(new Color(200, 30, 30));
            t.setBorder(BorderFactory.createEmptyBorder(20, 0, 5, 0));
            add(t, BorderLayout.NORTH);
            lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
            lista.setOpaque(false);
            JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
            centro.setOpaque(false);
            centro.add(lista);
            add(new JScrollPane(centro) {{ setBorder(null); getViewport().setOpaque(false); setOpaque(false); }}, BorderLayout.CENTER);

            btnAdd.setPreferredSize(new Dimension(300, 52));
            btnAdd.addActionListener(e -> { if (slots.size() < 5) { novoSlot(); refazer(); } });
            JPanel sul = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            sul.setOpaque(false);
            sul.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            sul.add(MenuPanel.botao("VOLTAR", new Color(90, 90, 100), () -> app.show(MENU)));
            sul.add(btnAdd);
            sul.add(MenuPanel.botao("COMEÇAR O JOGO", new Color(220, 40, 40), this::comecar));
            add(sul, BorderLayout.SOUTH);
            novoSlot(); novoSlot();
            refazer();
        }

        void novoSlot() {
            Slot s = new Slot();
            s.piloto = primeiroLivre(null);
            slots.add(s);
        }

        int primeiroLivre(Slot ignorar) {
            for (int i = 0; i < PILOTOS.length; i++) if (!usado(i, ignorar)) return i;
            return 0;
        }

        boolean usado(int idx, Slot ignorar) {
            for (Slot s : slots) if (s != ignorar && s.piloto == idx) return true;
            return false;
        }

        void refazer() {
            lista.removeAll();
            for (int i = 0; i < slots.size(); i++) {
                final Slot s = slots.get(i);
                final int n = i + 1;
                JPanel linha = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
                linha.setBackground(new Color(255, 250, 235));
                linha.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 160, 90), 2, true),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                JLabel num = new JLabel("Jogador " + n);
                num.setFont(new Font("SansSerif", Font.BOLD, 20));
                num.setPreferredSize(new Dimension(120, 30));
                s.nome.setFont(new Font("SansSerif", Font.PLAIN, 20));
                s.nome.setPreferredSize(new Dimension(260, 40));
                BotaoRedondo pb = new BotaoRedondo(PILOTOS[s.piloto].nome, PILOTOS[s.piloto].corpo.darker());
                pb.setFont(new Font("SansSerif", Font.BOLD, 17));
                pb.setIcon(new IconeCarro(PILOTOS[s.piloto], 70, 44));
                pb.setPreferredSize(new Dimension(310, 56));
                pb.addActionListener(e -> escolher(s));
                linha.add(num);
                linha.add(s.nome);
                linha.add(pb);
                if (slots.size() > 2) {
                    BotaoRedondo rm = new BotaoRedondo("X", new Color(150, 50, 50));
                    rm.setPreferredSize(new Dimension(50, 46));
                    rm.addActionListener(e -> { slots.remove(s); refazer(); });
                    linha.add(rm);
                }
                lista.add(linha);
                lista.add(Box.createVerticalStrut(10));
            }
            btnAdd.setEnabled(slots.size() < 5);
            lista.revalidate();
            lista.repaint();
        }

        void escolher(Slot s) {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Escolha seu personagem", Dialog.ModalityType.APPLICATION_MODAL);
            JPanel grade = new JPanel(new GridLayout(3, 4, 10, 10));
            grade.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            grade.setBackground(new Color(255, 238, 200));
            for (int i = 0; i < PILOTOS.length; i++) {
                final int idx = i;
                BotaoRedondo b = new BotaoRedondo(html(PILOTOS[i].nome), PILOTOS[i].corpo.darker());
                b.setFont(new Font("SansSerif", Font.BOLD, 14));
                b.setHorizontalTextPosition(SwingConstants.CENTER);
                b.setVerticalTextPosition(SwingConstants.BOTTOM);
                b.setIcon(new IconeCarro(PILOTOS[i], 80, 50));
                b.setPreferredSize(new Dimension(190, 110));
                b.setEnabled(!usado(i, s));
                b.addActionListener(e -> { s.piloto = idx; d.dispose(); });
                grade.add(b);
            }
            d.add(grade);
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            refazer();
        }

        void comecar() {
            List<Jogador> js = new ArrayList<>();
            int i = 1;
            for (Slot s : slots) {
                String n = s.nome.getText().trim();
                if (n.isEmpty()) n = "Jogador " + i;
                js.add(new Jogador(n, PILOTOS[s.piloto]));
                i++;
            }
            app.gamePanel.iniciar(js);
            app.show(GAME);
        }
    }

    // =====================================================================
    //  TELA DO JOGO (TABULEIRO)
    // =====================================================================

    static class GamePanel extends JPanel {
        List<Jogador> jogadores = new ArrayList<>();
        int atual = 0, chegaram = 0;
        boolean jogaDeNovo = false;
        Baralho<Pergunta> perguntas = new Baralho<>(criarPerguntas());
        Baralho<Carta> boas = new Baralho<>(cartasBoas());
        Baralho<Carta> ruins = new Baralho<>(cartasRuins());
        final Pista pista = new Pista();
        final JLabel vezLabel = new JLabel();
        final JPanel placar = new JPanel();
        final JTextArea log = new JTextArea(8, 20);
        final BotaoRedondo btn = new BotaoRedondo("PUXAR CARTA DE DESAFIO", new Color(220, 40, 40));

        GamePanel() {
            setLayout(new BorderLayout());
            add(pista, BorderLayout.CENTER);
            JPanel lado = new JPanel();
            lado.setLayout(new BoxLayout(lado, BoxLayout.Y_AXIS));
            lado.setBackground(new Color(255, 244, 215));
            lado.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            lado.setPreferredSize(new Dimension(330, 100));
            JLabel tit = new JLabel("RALI CARTESIANO");
            tit.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 26));
            tit.setForeground(new Color(200, 30, 30));
            tit.setAlignmentX(0f);
            vezLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
            vezLabel.setAlignmentX(0f);
            placar.setLayout(new BoxLayout(placar, BoxLayout.Y_AXIS));
            placar.setOpaque(false);
            placar.setAlignmentX(0f);
            log.setEditable(false); log.setLineWrap(true); log.setWrapStyleWord(true);
            log.setFont(new Font("SansSerif", Font.PLAIN, 13));
            JScrollPane sp = new JScrollPane(log);
            sp.setAlignmentX(0f);
            btn.setAlignmentX(0f);
            btn.setFont(new Font("SansSerif", Font.BOLD, 17));
            btn.setMaximumSize(new Dimension(300, 60));
            btn.addActionListener(e -> jogar());
            lado.add(tit);
            lado.add(Box.createVerticalStrut(10));
            lado.add(vezLabel);
            lado.add(Box.createVerticalStrut(10));
            lado.add(placar);
            lado.add(Box.createVerticalStrut(10));
            lado.add(sp);
            lado.add(Box.createVerticalStrut(12));
            lado.add(btn);
            add(lado, BorderLayout.EAST);
        }

        void iniciar(List<Jogador> js) {
            jogadores = js;
            atual = 0; chegaram = 0; jogaDeNovo = false;
            log.setText("");
            pista.jogadores = js;
            pista.atual = js.get(0);
            perguntas = new Baralho<>(criarPerguntas());
            boas = new Baralho<>(cartasBoas());
            ruins = new Baralho<>(cartasRuins());
            registrar("Largada! Boa corrida a todos!");
            atualizarLado();
            btn.setEnabled(true);
        }

        void registrar(String s) { log.append(s + "\n"); log.setCaretPosition(log.getDocument().getLength()); }

        Jogador atualJ() { return jogadores.get(atual); }

        void atualizarLado() {
            Jogador j = atualJ();
            pista.atual = j;
            vezLabel.setText("Vez de: " + j.nome);
            vezLabel.setIcon(new IconeCarro(j.piloto, 64, 40));
            placar.removeAll();
            for (Jogador x : jogadores) {
                String st = x.rank > 0 ? x.rank + "º lugar!" : "casa " + x.pos + "/" + FINISH;
                if (x.escudo) st += "  [escudo]";
                if (x.parado > 0) st += "  [parado]";
                JLabel l = new JLabel(x.nome + " - " + st);
                l.setFont(new Font("SansSerif", x == j ? Font.BOLD : Font.PLAIN, 14));
                l.setIcon(new IconeCarro(x.piloto, 44, 28));
                l.setAlignmentX(0f);
                placar.add(l);
            }
            placar.revalidate();
            placar.repaint();
            pista.repaint();
        }

        Window janela() { return SwingUtilities.getWindowAncestor(this); }

        void jogar() {
            btn.setEnabled(false);
            Jogador j = atualJ();
            Pergunta q = perguntas.proxima();
            PerguntaDialog pd = new PerguntaDialog(janela(), q, j);
            pd.setVisible(true);
            Carta c;
            if (pd.acertou) {
                c = boas.proxima();
                registrar(j.nome + " acertou!");
                if (c.titulo.startsWith("Turbo")) Som.kachow();
                else Som.acerto();
                CartaDialog.mostrar(this, "RESPOSTA CERTA!", new Color(40, 150, 70),
                        c.titulo, c.msg, j.piloto);


                CartaDialog.mostrar(this, "RESPOSTA CERTA!", new Color(40, 150, 70),
                        c.titulo, c.msg, j.piloto);
            } else {
                registrar(j.nome + " errou.");
                Som.erro();
                CartaDialog.mostrar(this, "NÃO FOI DESSA VEZ...", new Color(200, 70, 40),
                        "Veja como se resolve", "A resposta certa era: " + q.certa + "\n\n" + q.exp, j.piloto);
                c = ruins.proxima();
                if (j.escudo) {
                    j.escudo = false;
                    registrar(j.nome + " usou o escudo e ignorou a carta \"" + c.titulo + "\".");
                    CartaDialog.mostrar(this, "CARTA NEGATIVA", new Color(120, 60, 60), c.titulo,
                            c.msg + "\n\nMas seu ESCUDO protegeu você! Nada acontece.", j.piloto);
                    c = null;
                } else {
                    CartaDialog.mostrar(this, "CARTA NEGATIVA", new Color(150, 40, 40), c.titulo, c.msg, j.piloto);
                }
            }
            if (c != null) {
                aplicar(j, c);
                if (c.ef == Efeito.AVANCAR) Som.motor();
                else if (c.ef == Efeito.VOLTAR) Som.derrapa();
            }
            atualizarLado();
            esperarAnimacao(() -> fimDaJogada(j));
        }

        void aplicar(Jogador j, Carta c) {
            switch (c.ef) {
                case AVANCAR:
                    j.pos = Math.min(FINISH, j.pos + c.n);
                    registrar(j.nome + " avança " + c.n + " casa(s).");
                    break;
                case VOLTAR:
                    j.pos = Math.max(0, j.pos - c.n);
                    registrar(j.nome + " volta " + c.n + " casa(s).");
                    break;
                case PARADO:
                    j.parado += c.n;
                    registrar(j.nome + " ficará " + c.n + " rodada(s) sem jogar.");
                    break;
                case JOGAR_DE_NOVO:
                    jogaDeNovo = true;
                    registrar(j.nome + " joga de novo.");
                    break;
                case ESCUDO:
                    j.escudo = true;
                    registrar(j.nome + " ganhou um escudo.");
                    break;
                case TROCA_LIDER: {
                    Jogador lider = null;
                    for (Jogador o : jogadores) if (o != j && o.rank == 0 && (lider == null || o.pos > lider.pos)) lider = o;
                    if (lider != null && lider.pos > j.pos) {
                        int tmp = j.pos; j.pos = lider.pos; lider.pos = tmp;
                        registrar(j.nome + " trocou de lugar com " + lider.nome + " (líder).");
                    } else {
                        j.pos = Math.min(FINISH, j.pos + 2);
                        registrar(j.nome + " já é o líder e avança 2 casas.");
                    }
                    break;
                }
                case TROCA_ULTIMO: {
                    Jogador ult = null;
                    for (Jogador o : jogadores) if (o != j && o.rank == 0 && (ult == null || o.pos < ult.pos)) ult = o;
                    if (ult != null && ult.pos < j.pos) {
                        int tmp = j.pos; j.pos = ult.pos; ult.pos = tmp;
                        registrar(j.nome + " trocou de lugar com " + ult.nome + " (último).");
                    } else {
                        j.pos = Math.max(0, j.pos - 1);
                        registrar(j.nome + " já é o último e volta 1 casa.");
                    }
                    break;
                }
                case EMPURRAR: {
                    List<Jogador> alvos = new ArrayList<>();
                    for (Jogador o : jogadores) if (o != j && o.rank == 0) alvos.add(o);
                    if (alvos.isEmpty()) break;
                    String[] nomes = new String[alvos.size()];
                    for (int i = 0; i < nomes.length; i++) nomes[i] = alvos.get(i).nome;
                    int r = -1;
                    while (r < 0) {
                        r = JOptionPane.showOptionDialog(this, "Quem vai voltar " + c.n + " casas?", "Pegadinha!",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, nomes, nomes[0]);
                    }
                    Jogador alvo = alvos.get(r);
                    alvo.pos = Math.max(0, alvo.pos - c.n);
                    registrar(j.nome + " fez uma pegadinha: " + alvo.nome + " volta " + c.n + " casas.");
                    break;
                }
            }
        }

        void esperarAnimacao(Runnable r) {
            Timer t = new Timer(50, null);
            t.addActionListener(e -> { if (pista.parada()) { t.stop(); r.run(); } });
            t.start();
        }

        void fimDaJogada(Jogador j) {
            if (j.pos >= FINISH && j.rank == 0) {
                j.rank = ++chegaram;
                registrar(j.nome + " CRUZOU A LINHA DE CHEGADA em " + j.rank + "º lugar!");
                Som.vitoria();
                CartaDialog.mostrar(this, "LINHA DE CHEGADA!", new Color(40, 100, 200),
                        j.nome + " chegou em " + j.rank + "º lugar!", "Parabéns, " + j.nome + "! Kachow!", j.piloto);
            }
            int restantes = 0;
            Jogador ultimo = null;
            for (Jogador o : jogadores) if (o.rank == 0) { restantes++; ultimo = o; }
            if (restantes <= 1) {
                if (ultimo != null) ultimo.rank = ++chegaram;
                atualizarLado();
                app.podiumPanel.mostrarResultado(jogadores);
                app.show(PODIUM);
                return;
            }
            if (!jogaDeNovo || j.rank > 0) proximo();
            jogaDeNovo = false;
            atualizarLado();
            btn.setEnabled(true);
        }

        void proximo() {
            while (true) {
                atual = (atual + 1) % jogadores.size();
                Jogador q = jogadores.get(atual);
                if (q.rank != 0) continue;
                if (q.parado > 0) { q.parado--; registrar(q.nome + " está parado(a) e perde a vez."); continue; }
                break;
            }
        }
    }

    /** Desenho da pista de corrida (tabuleiro). */
    /** Desenho da pista de corrida (tabuleiro). */
    static class Pista extends JPanel {
        static final int COLS = 6;                          // casas por linha
        static final int ROWS = (FINISH + COLS - 1) / COLS; // linhas do tabuleiro
        List<Jogador> jogadores = new ArrayList<>();
        Jogador atual;
        double cw, ch;
        BufferedImage fundo;   // cache do tabuleiro (não muda durante o jogo)
        final double[][] OFF = {{-.22, -.2}, {.22, -.2}, {-.22, .2}, {.22, .2}, {0, 0}};

        Pista() {
            setPreferredSize(new Dimension(950, 700));
            new Timer(25, e -> {
                for (Jogador j : jogadores) {
                    double d = j.pos - j.vis;
                    if (Math.abs(d) < 0.01) {
                        j.vis = j.pos;
                    } else {
                        double passo = Math.min(Math.abs(d), Math.max(0.10, Math.abs(d) * 0.06));
                        j.vis += Math.signum(d) * passo;
                    }
                }
                if (isShowing()) repaint();
            }).start();
        }

        boolean parada() {
            for (Jogador j : jogadores) if (j.vis != j.pos) return false;
            return true;
        }

        Point2D.Double centro(int i) {
            if (i <= 0) return new Point2D.Double(cw / 2, ch / 2);
            int n = i - 1, r = n / COLS, k = n % COLS, col = (r % 2 == 0) ? k : COLS - 1 - k;
            return new Point2D.Double((col + 1) * cw + cw / 2, r * ch + ch / 2);
        }

        Point2D.Double posCarro(int casa, int idx) {
            if (casa <= 0) {   // largada: grade de 2 colunas
                int col = idx % 2, lin = idx / 2;
                double px = (idx == 4) ? 0.5 : (col == 0 ? 0.27 : 0.73);
                return new Point2D.Double(cw * px, ch * (0.2 + 0.23 * lin));
            }
            Point2D.Double c = centro(casa);
            return new Point2D.Double(c.x + OFF[idx][0] * cw, c.y + OFF[idx][1] * ch);
        }

        void desenharFundo(Graphics2D g, int w, int h) {
            g.setPaint(new GradientPaint(0, 0, new Color(245, 215, 150), 0, h, new Color(225, 170, 100)));
            g.fillRect(0, 0, w, h);
            // cenário
            g.setColor(new Color(190, 105, 65, 150));
            mesa(g, 5, ch * 2 - 10, cw * 0.95, ch * 0.7);
            g.setColor(new Color(170, 90, 55, 150));
            mesa(g, 5, ch * 3.95, cw * 0.95, ch * 0.8);
            cacto(g, cw * 0.5, ch * 2.9, cw / 90.0 * 1.2);
            cacto(g, cw * 0.65, ch * 4.9, cw / 90.0 * 1.1);
            int pl = (int) (cw * 0.06), pw = (int) (cw * 0.88), pt = (int) ch + 14, ph = 50;
            g.setColor(new Color(120, 70, 40));
            g.fillRect((int) (cw * 0.5) - 4, pt + ph, 8, 30);
            g.fillRoundRect(pl, pt, pw, ph, 10, 10);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, (int) Math.max(11, cw / 9)));
            FontMetrics fmp = g.getFontMetrics();
            g.drawString("Radiator", pl + (pw - fmp.stringWidth("Radiator")) / 2, pt + 21);
            g.drawString("Springs", pl + (pw - fmp.stringWidth("Springs")) / 2, pt + 42);
            // largada
            g.setColor(new Color(60, 140, 70));
            g.fillRoundRect(4, 4, (int) cw - 8, (int) ch - 8, 18, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, (int) Math.max(12, cw / 7)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("LARGADA", (int) (cw / 2 - fm.stringWidth("LARGADA") / 2), (int) (ch - 16));

            // objetos reaproveitados em todas as casas
            Font fNum = new Font("SansSerif", Font.BOLD, (int) Math.max(12, cw / 5.5));
            Font fChegada = new Font("SansSerif", Font.BOLD, (int) Math.max(11, cw / 7));
            Stroke tracejado = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{10f, 9f}, 0f);
            Stroke fino = new BasicStroke(1f), grosso = new BasicStroke(3f);
            Color asfalto = new Color(72, 74, 84), vermelho = new Color(220, 40, 40);
            Color amareloLinha = new Color(255, 220, 80, 170), numCor = new Color(255, 255, 255, 235);

            // casas
            for (int i = 1; i <= FINISH; i++) {
                Point2D.Double c = centro(i);
                int x = (int) (c.x - cw / 2) + 3, y = (int) (c.y - ch / 2) + 3, tw = (int) cw - 6, th = (int) ch - 6;
                if (i == FINISH) {
                    int n = 6; double sq = tw / (double) n;
                    int rows = Math.max(1, (int) Math.round(th / sq));
                    double sqh = th / (double) rows;
                    for (int rr = 0; rr < rows; rr++) for (int cc = 0; cc < n; cc++) {
                        g.setColor((rr + cc) % 2 == 0 ? Color.BLACK : Color.WHITE);
                        g.fill(new Rectangle2D.Double(x + cc * sq, y + rr * sqh, sq + 1, sqh + 1));
                    }
                    g.setColor(new Color(255, 255, 255, 230));
                    g.fillRoundRect(x + 6, y + th / 2 - 14, tw - 12, 28, 10, 10);
                    g.setColor(Color.BLACK);
                    g.setFont(fChegada);
                    FontMetrics f2 = g.getFontMetrics();
                    g.drawString("CHEGADA", x + (tw - f2.stringWidth("CHEGADA")) / 2, y + th / 2 + 5);
                    continue;
                }
                g.setColor(asfalto);
                g.fillRoundRect(x, y, tw, th, 16, 16);
                // zebra nas bordas
                for (int k = 0; k < tw; k += 14) {
                    g.setColor(((k / 14) % 2 == 0) ? vermelho : Color.WHITE);
                    g.fillRect(x + k + 4, y + 2, Math.min(14, tw - k - 8), 4);
                    g.fillRect(x + k + 4, y + th - 6, Math.min(14, tw - k - 8), 4);
                }
                // linha central tracejada
                g.setColor(amareloLinha);
                g.setStroke(tracejado);
                g.drawLine(x + 6, (int) c.y, x + tw - 6, (int) c.y);
                g.setStroke(fino);
                if (i % 10 == 0) {
                    g.setColor(new Color(255, 200, 0));
                    g.setStroke(grosso);
                    g.drawRoundRect(x, y, tw, th, 16, 16);
                    g.setStroke(fino);
                }
                g.setColor(numCor);
                g.setFont(fNum);
                g.drawString("" + i, x + 8, y + 24);
            }
        }

        @Override protected void paintComponent(Graphics g0) {
            int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            cw = w / (COLS + 1.0);
            ch = h / (double) ROWS;
            // o fundo só é redesenhado quando a janela muda de tamanho
            if (fundo == null || fundo.getWidth() != w || fundo.getHeight() != h) {
                fundo = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D gb = fundo.createGraphics();
                aa(gb);
                desenharFundo(gb, w, h);
                gb.dispose();
            }
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            g.drawImage(fundo, 0, 0, null);

            // carros
            long agora = System.currentTimeMillis();
            double s = Math.min(cw / 160.0, ch / 110.0);
            for (int idx = 0; idx < jogadores.size(); idx++) {
                Jogador j = jogadores.get(idx);
                int a = (int) Math.floor(j.vis), b = (int) Math.ceil(j.vis);
                Point2D.Double pa = posCarro(a, idx), pb = posCarro(b, idx);
                double f = j.vis - a;
                double x = pa.x + (pb.x - pa.x) * f;
                double y = pa.y + (pb.y - pa.y) * f;
                y += Math.sin(agora / 180.0 + idx * 1.7) * 1.5;
                int linha = Math.max(0, (int) (Math.round(j.vis) - 1) / COLS);
                boolean esq = Math.round(j.vis) >= 1 && linha % 2 == 1;
                desenharCarro(g, x, y, s, j.piloto, esq, 0);
                if (j == atual) {
                    g.setColor(new Color(255, 230, 0));
                    int[] xs = {(int) x - 7, (int) x + 7, (int) x};
                    int ya = (int) (y - 30 * s) - 12;
                    int[] ys = {ya, ya, ya + 10};
                    g.fillPolygon(xs, ys, 3);
                    g.setColor(Color.BLACK);
                    g.drawPolygon(xs, ys, 3);
                }
                if (j.escudo) {
                    g.setColor(new Color(80, 190, 255, 120));
                    g.fill(new Ellipse2D.Double(x - 34 * s, y - 28 * s, 68 * s, 56 * s));
                }
            }
            g.dispose();
        }
    }
    // =====================================================================
    //  TELA DO PÓDIO
    // =====================================================================

    static class PodiumPanel extends JPanel {
        List<Jogador> ordem = new ArrayList<>();
        final List<double[]> confete = new ArrayList<>();
        final Random rnd = new Random();
        final Timer timer;

        PodiumPanel() {
            setLayout(new BorderLayout());
            JPanel sul = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
            sul.setOpaque(false);
            sul.add(MenuPanel.botao("JOGAR DE NOVO", new Color(60, 150, 90), () -> app.show(SETUP)));
            sul.add(MenuPanel.botao("MENU PRINCIPAL", new Color(30, 120, 200), () -> app.show(MENU)));
            add(sul, BorderLayout.SOUTH);
            timer = new Timer(30, e -> {
                for (double[] c : confete) {
                    c[1] += c[3];
                    c[0] += Math.sin(c[1] / 30.0 + c[4]) * 0.8;
                    c[5] += 0.1;
                    if (c[1] > getHeight()) { c[1] = -10; c[0] = rnd.nextInt(Math.max(1, getWidth())); }
                }
                if (isShowing()) repaint();
            });
        }

        void mostrarResultado(List<Jogador> js) {
            ordem = new ArrayList<>(js);
            ordem.sort(Comparator.comparingInt(j -> j.rank));
            confete.clear();
            for (int i = 0; i < 120; i++)
                confete.add(new double[]{rnd.nextInt(1200), rnd.nextInt(700), rnd.nextInt(6) + 5, 2 + rnd.nextDouble() * 3, rnd.nextDouble() * 6, 0, rnd.nextInt(6)});
            Som.vitoria();
            timer.start();
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            aa(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(25, 20, 70), 0, h, new Color(120, 60, 150)));
            g.fillRect(0, 0, w, h);
            // holofote
            g.setColor(new Color(255, 240, 160, 40));
            Path2D lz = new Path2D.Double();
            lz.moveTo(w / 2.0 - 40, 0); lz.lineTo(w / 2.0 + 40, 0);
            lz.lineTo(w / 2.0 + 260, h * 0.8); lz.lineTo(w / 2.0 - 260, h * 0.8);
            lz.closePath();
            g.fill(lz);
            g.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 54));
            MenuPanel.texto(g, "FIM DE CORRIDA!", w / 2, 70, new Color(255, 215, 0));

            double chao = h * 0.78;
            double bw = w * 0.17, cx = w / 2.0;
            double sc = Math.min(w, h) / 300.0;
            long agora = System.currentTimeMillis();
            // chão
            g.setColor(new Color(60, 50, 90));
            g.fillRect(0, (int) chao, w, h - (int) chao);
            g.setColor(new Color(255, 255, 255, 60));
            g.drawLine(0, (int) chao, w, (int) chao);

            double[] hs = {h * 0.30, h * 0.21, h * 0.14};
            double[] xs = {cx - bw / 2, cx - bw * 1.5, cx + bw / 2};
            Color[] cs = {new Color(240, 190, 40), new Color(190, 195, 205), new Color(200, 125, 60)};
            for (int i = 0; i < Math.min(3, ordem.size()); i++) {
                Jogador j = ordem.get(i);
                double top = chao - hs[i];
                g.setPaint(new GradientPaint(0, (float) top, cs[i].brighter(), 0, (float) chao, cs[i].darker()));
                g.fill(new RoundRectangle2D.Double(xs[i], top, bw, hs[i], 10, 10));
                g.setColor(new Color(255, 255, 255, 230));
                g.setFont(new Font("SansSerif", Font.BOLD, (int) (bw * 0.45)));
                String num = "" + (i + 1);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(num, (int) (xs[i] + (bw - fm.stringWidth(num)) / 2), (int) (top + bw * 0.5));
                g.setFont(new Font("SansSerif", Font.BOLD, 17));
                fm = g.getFontMetrics();
                String nome = j.nome.length() > 14 ? j.nome.substring(0, 13) + "." : j.nome;
                g.setColor(Color.BLACK);
                g.drawString(nome, (int) (xs[i] + (bw - fm.stringWidth(nome)) / 2), (int) (chao - 12));
                double bob = i == 0 ? Math.sin(agora / 200.0) * 5 : 0;
                double carX = xs[i] + bw / 2, carY = top - 22 * sc - 2 + bob;
                if (i == 0) {
                    // braço segurando o troféu
                    g.setColor(new Color(40, 40, 50));
                    g.setStroke(new BasicStroke((float) (3.5 * sc), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    double hx = carX + 22 * sc, hy = carY - 36 * sc;
                    g.draw(new Line2D.Double(carX + 8 * sc, carY - 6 * sc, hx, hy));
                    desenharTrofeu(g, hx, hy - 1, sc * 1.1);
                    g.setColor(new Color(40, 40, 50));
                    g.fill(new Ellipse2D.Double(hx - 4 * sc, hy - 4 * sc, 8 * sc, 8 * sc));
                }
                desenharCarro(g, carX, carY, sc, j.piloto, false, 0);
            }
            // demais jogadores: no chão, chorando
            for (int i = 3; i < ordem.size(); i++) {
                Jogador j = ordem.get(i);
                double s2 = sc * 0.8;
                double lado = (i == 3) ? -1 : 1;
                double carX = cx + lado * (bw * 2.55);
                double carY = chao - 21 * s2 + 4;
                desenharCarro(g, carX, carY, s2, j.piloto, lado > 0, 1);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g.getFontMetrics();
                String t = (i + 1) + "º - " + j.nome;
                g.setColor(Color.WHITE);
                g.drawString(t, (int) (carX - fm.stringWidth(t) / 2.0), (int) (chao + 28));
            }
            // confete
            Color[] cc = {Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE};
            for (double[] c : confete) {
                g.setColor(cc[(int) c[6]]);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.translate(c[0], c[1]);
                g2.rotate(c[5]);
                g2.fillRect(-3, -2, 7, 4);
                g2.dispose();
            }
            g.dispose();
        }
    }
}
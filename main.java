import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// Interfejs reprezentujący przechowywanie funkcji
interface PrzechowywanieFunkcji {
    void zapiszFunkcje(List<Funkcja> funkcje);
    List<Funkcja> wczytajFunkcje();
    List<Funkcja> wyszukajFunkcje(double pierwiastek);
}

// Klasa reprezentująca przechowywanie funkcji w pliku
class PlikowePrzechowywanieFunkcji implements PrzechowywanieFunkcji {
    private static final String NAZWA_PLIKU = "funkcje.txt";

    @Override
    public void zapiszFunkcje(List<Funkcja> funkcje) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(NAZWA_PLIKU))) {
            outputStream.writeObject(funkcje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Funkcja> wczytajFunkcje() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(NAZWA_PLIKU))) {
            return (List<Funkcja>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Funkcja> wyszukajFunkcje(double pierwiastek) {
        List<Funkcja> znalezioneFunkcje = new ArrayList<>();
        List<Funkcja> funkcje = wczytajFunkcje();
        for (Funkcja funkcja : funkcje) {
            if (funkcja.obliczPierwiastek() == pierwiastek) {
                znalezioneFunkcje.add(funkcja);
            }
        }
        return znalezioneFunkcje;
    }
}

// Abstrakcyjna klasa bazowa reprezentująca formularz funkcji
abstract class FormularzFunkcji extends JFrame implements ActionListener {
    protected PrzechowywanieFunkcji przechowywanieFunkcji;

    protected JTextField wspolczynnikAField;
    protected JTextField wyrazWolnyField;
    protected JButton obliczButton;
    protected JButton zapiszButton;
    protected JTextArea wynikArea;

    public FormularzFunkcji(PrzechowywanieFunkcji przechowywanieFunkcji) {
        this.przechowywanieFunkcji = przechowywanieFunkcji;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Kalkulator Funkcji");
        setLayout(new FlowLayout());

        JLabel wspolczynnikALabel = new JLabel("Współczynnik a:");
        JLabel wyrazWolnyLabel = new JLabel("Wyraz wolny:");

        wspolczynnikAField = new JTextField(10);
        wyrazWolnyField = new JTextField(10);

        obliczButton = new JButton("Oblicz");
        zapiszButton = new JButton("Zapisz");

        wynikArea = new JTextArea(10, 20);
        wynikArea.setEditable(false);

        obliczButton.addActionListener(this);
        zapiszButton.addActionListener(this);

        add(wspolczynnikALabel);
        add(wspolczynnikAField);
        add(wyrazWolnyLabel);
        add(wyrazWolnyField);
        add(obliczButton);
        add(zapiszButton);
        add(wynikArea);

        pack();
        setVisible(true);
    }

    protected abstract void obliczFunkcje();

    protected Funkcja utworzFunkcje() {
        double wspolczynnikA = Double.parseDouble(wspolczynnikAField.getText());
        double wyrazWolny = Double.parseDouble(wyrazWolnyField.getText());
        return new Funkcja(wspolczynnikA, wyrazWolny);
    }

    protected void wyswietlWynik(Funkcja funkcja) {
        String wynik = "Miejsce zerowe: " + funkcja.obliczPierwiastek() + "\n";
        wynik += "Punkt przecięcia z osią OY: " + funkcja.obliczPunktPrzecieciaYOX();
        wynikArea.setText(wynik);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == obliczButton) {
            obliczFunkcje();
        } else if (e.getSource() == zapiszButton) {
            Funkcja funkcja = utworzFunkcje();
            zapiszFunkcje(funkcja);
        }
    }

    protected void zapiszFunkcje(Funkcja funkcja) {
        List<Funkcja> funkcje = przechowywanieFunkcji.wczytajFunkcje();
        funkcje.add(funkcja);
        przechowywanieFunkcji.zapiszFunkcje(funkcje);
    }
}

// Klasa reprezentująca formularz kalkulatora funkcji
class FormularzKalkulatoraFunkcji extends FormularzFunkcji {
    public FormularzKalkulatoraFunkcji(PrzechowywanieFunkcji przechowywanieFunkcji) {
        super(przechowywanieFunkcji);
    }

    @Override
    protected void obliczFunkcje() {
        Funkcja funkcja = utworzFunkcje();
        wyswietlWynik(funkcja);
    }
}

// Klasa reprezentująca formularz wyszukiwania funkcji
class FormularzWyszukiwaniaFunkcji extends FormularzFunkcji {
    private JTextField pierwiastekField;
    private JButton wyszukajButton;

    public FormularzWyszukiwaniaFunkcji(PrzechowywanieFunkcji przechowywanieFunkcji) {
        super(przechowywanieFunkcji);
        pierwiastekField = new JTextField(10);
        wyszukajButton = new JButton("Wyszukaj");
        wyszukajButton.addActionListener(this);
        add(new JLabel("Pierwiastek:"));
        add(pierwiastekField);
        add(wyszukajButton);
    }

    @Override
    protected void obliczFunkcje() {
        double pierwiastek = Double.parseDouble(pierwiastekField.getText());
        List<Funkcja> funkcje = przechowywanieFunkcji.wyszukajFunkcje(pierwiastek);
        if (funkcje.isEmpty()) {
            wynikArea.setText("Brak funkcji o podanym pierwiastku.");
        } else {
            StringBuilder wynik = new StringBuilder();
            wynik.append("Znalezione funkcje:\n");
            for (Funkcja funkcja : funkcje) {
                wynik.append("Funkcja: ").append(funkcja).append("\n");
            }
            wynikArea.setText(wynik.toString());
        }
    }
}

// Klasa reprezentująca funkcję liniową
class Funkcja implements Serializable {
    private static final long serialVersionUID = 1L;

    private double wspolczynnikA;
    private double wyrazWolny;

    public Funkcja(double wspolczynnikA, double wyrazWolny) {
        this.wspolczynnikA = wspolczynnikA;
        this.wyrazWolny = wyrazWolny;
    }

    public double obliczPierwiastek() {
        return -wyrazWolny / wspolczynnikA;
    }

    public double obliczPunktPrzecieciaYOX() {
        return wyrazWolny;
    }

    @Override
    public String toString() {
        return "y = " + wspolczynnikA + "x + " + wyrazWolny;
    }
}

// Klasa reprezentująca aplikację kalkulatora funkcji
public class main {
    public static void main(String[] args) {
        PrzechowywanieFunkcji przechowywanieFunkcji = new PlikowePrzechowywanieFunkcji();

        FormularzKalkulatoraFunkcji formularzKalkulatora = new FormularzKalkulatoraFunkcji(przechowywanieFunkcji);
        FormularzWyszukiwaniaFunkcji formularzWyszukiwania = new FormularzWyszukiwaniaFunkcji(przechowywanieFunkcji);
    }
}

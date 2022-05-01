package pianomix;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.AudioSample;
import com.jsyn.data.FloatSample;
import com.jsyn.data.ShortSample;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.MixerStereo;
import com.jsyn.unitgen.MixerStereoRamped;
import com.jsyn.unitgen.MultiplyAdd;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillator;
import com.jsyn.unitgen.VariableRateMonoReader;
import com.jsyn.unitgen.VariableRateStereoReader;
import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveRecorder;
import com.softsynth.shared.time.TimeStamp;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Egor Chekunov
 * https://www.geeksforgeeks.org/play-audio-file-using-java/
 *
 *
 * https://docs.oracle.com/javase/tutorial/sound/MIDI-synth.html
 *
 *
 *
 * https://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/Mixer.html
 *
 *
 * http://www.softsynth.com/jsyn/tutorial/howto_tut.php
 *
 * https://en.wikipedia.org/wiki/Musical_note ukoly ↓
 *
 *
 *
 *
 * 4. menit kurzor pri roztahovanis 5. pretahovat notu a mnenit jeji x,y
 *
 *
 */
public class PianoMix {

    JFrame frame = new JFrame();
    JFrame saveWindow;
    JPanel panel;

    static Boolean[] kick;
    static Boolean[] hihat;
    static Boolean[] snare;
    
    static JButton[] kickB  = new MyButton[32];
    static JButton[] hihatB = new MyButton[32];
    static JButton[] snareB = new MyButton[32];
        
    static Share share = new Share();
    static ArrayList<Note> notes = new ArrayList<Note>();
    static Clip clip;

    static JButton saveB;
    static JButton startB;
    static JButton upB;
    
    static Color myActiveColor = new Color(13, 41, 62);
    static Color myDefaultColor = new Color(69, 74, 73);
    
    static JTextField textField;
    
    static double longestStamp = 0;

    boolean RUN = true;
    
    static Synthesizer synth = JSyn.createSynthesizer();
    static ArrayList<SquareOscillator> oscillator = new ArrayList<>();
    static ArrayList<LineOut> lineOut = new ArrayList<>();
    
    static String saveFileName="NewFile";
    
    static File fileHihat = new File("program files//snd//hihat.wav");
    static File fileKick = new File("program files//snd//kick.wav");
    static File fileSnare = new File("program files//snd//snare.wav");

    static AudioSample hihatSample;
    static AudioSample kickSample;
    static AudioSample snareSample;
    static VariableRateStereoReader samplePlayerHihat = new VariableRateStereoReader();
    static VariableRateStereoReader samplePlayerKick = new VariableRateStereoReader();
    static VariableRateStereoReader samplePlayerSnare = new VariableRateStereoReader();
    static LineOut lineOutHihat = new LineOut();
    static LineOut lineOutKick = new LineOut();
    static LineOut lineOutSnare = new LineOut();
    
    static Gson gson = new Gson();
    static Scanner sc = new Scanner(System.in);
    
    MouseListener mou = new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

            if (e.getButton() == MouseEvent.BUTTON1) {
                share.MBleft = true;
            }
            if (e.getButton() == MouseEvent.BUTTON3) {
                share.MBleft = false;
            }
            share.mousePressed = true;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            share.mouseReleased = true;

        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    };

    BufferedImage kickP = ImageIO.read(new File("program files//pic//kick.png"));
    BufferedImage hihatP = ImageIO.read(new File("program files//pic//hihat.png"));
    BufferedImage snareP = ImageIO.read(new File("program files//pic//snare.png"));
    BufferedImage logoP = ImageIO.read(new File("program files//pic//pianomixLogo.png"));

    AudioInputStream hihatAudio;

    public PianoMix() throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        for (int i = 0; i < 24; i++) {
            lineOut.add(new LineOut());
            oscillator.add(new SquareOscillator());
        }
        for (int i = 0; i < 24; i++) {

            synth.add(lineOut.get(i));
            synth.add(oscillator.get(i));

            oscillator.get(i).output.connect(0, lineOut.get(i).input, 0);
            oscillator.get(i).output.connect(0, lineOut.get(i).input, 1);
            double frequency = Math.pow(2.0, (14.0 - i) / 12.0) * 440.0;
            oscillator.get(i).frequency.set(frequency);
           
            oscillator.get(i).amplitude.set(0.02);
        }
        hihatSample = SampleLoader.loadFloatSample(fileHihat);
        kickSample = SampleLoader.loadFloatSample(fileKick);
        snareSample = SampleLoader.loadFloatSample(fileSnare);

        samplePlayerHihat.output.connect(0, lineOutHihat.input, 0);
        samplePlayerHihat.output.connect(0, lineOutHihat.input, 1);

        samplePlayerKick.output.connect(0, lineOutKick.input, 0);
        samplePlayerKick.output.connect(0, lineOutKick.input, 1);

        samplePlayerSnare.output.connect(0, lineOutSnare.input, 0);
        samplePlayerSnare.output.connect(0, lineOutSnare.input, 1);

        synth.add(lineOutHihat);
        synth.add(lineOutKick);
        synth.add(lineOutSnare);

        synth.add(samplePlayerKick);
        synth.add(samplePlayerHihat);
        synth.add(samplePlayerSnare);
        frame.getContentPane().setPreferredSize(new Dimension(1080, 720 + 48));
        frame.pack();
        frame.setTitle("Pianomix");

        frame.setResizable(false);
        Runnable run = new Runnable() {
            @Override
            public void run() {

                //vykresleni
                panel = new JPanel() {
                    @Override
                    public void setBackground(Color bg) {
                        super.setBackground(new Color(43, 43, 43)); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        //first 3 buttons
                        g.setColor(Color.white);
                        g.drawImage(logoP, 0, 0, null);
                        for (int i = 0; i < 3; i++) {
                            g.drawRect(0, i * 48 + 48, 100, 48);
                            if (i == 0) {
                                g.drawImage(kickP, 0, i * 48 + 48, null);
                            }
                            if (i == 1) {
                                g.drawImage(hihatP, 0, i * 48 + 48, null);
                            }
                            if (i == 2) {
                                g.drawImage(snareP, 0, i * 48 + 48, null);
                            }

                        }
                        //tones name
                        g.setFont(new Font(Font.SERIF, Font.BOLD, 25));
                        int n = 9;
                        g.drawString("h2", 20, n * 24);
                        n++;
                        g.drawString("a#2", 20, n * 24);
                        n++;
                        g.drawString("a2", 20, n * 24);
                        n++;
                        g.drawString("g#2", 20, n * 24);
                        n++;
                        g.drawString("g2", 20, n * 24);
                        n++;
                        g.drawString("f#2", 20, n * 24);
                        n++;
                        g.drawString("f2", 20, n * 24);
                        n++;
                        g.drawString("e2", 20, n * 24);
                        n++;
                        g.drawString("d#2", 20, n * 24);
                        n++;
                        g.drawString("d2", 20, n * 24);
                        n++;
                        g.drawString("c#2", 20, n * 24);
                        n++;
                        g.drawString("c2", 20, n * 24);
                        n++;
                        g.drawString("h1", 20, n * 24);
                        n++;
                        g.drawString("a#1", 20, n * 24);
                        n++;
                        g.drawString("a1", 20, n * 24);
                        n++;
                        g.drawString("g#1", 20, n * 24);
                        n++;
                        g.drawString("g1", 20, n * 24);
                        n++;
                        g.drawString("f#1", 20, n * 24);
                        n++;
                        g.drawString("f1", 20, n * 24);
                        n++;
                        g.drawString("e1", 20, n * 24);
                        n++;
                        g.drawString("d#1", 20, n * 24);
                        n++;
                        g.drawString("d1", 20, n * 24);
                        n++;
                        g.drawString("c#1", 20, n * 24);
                        n++;
                        g.drawString("c1", 20, n * 24);
                        n++;

                        //piano tones
                        for (int i = 0; i < 24; i++) {

                            for (int j = 0; j < 16; j++) {

                                g.drawRect(100 + j * 60, i * 24 + 192, 60, 24);

                            }
                        }
                        for (int i = 0; i < 24; i++) {
                            g.drawRect(0, i * 24 + 192, 100, 24);
                        }
//                        beat Boxes
                        g.setColor(Color.blue);
                        for (int j = 0; j < 3; j++) {

                            for (int i = 0; i < 32; i++) {

                                //vyplnovani kazde 4ty
                                if (i % 4 == 0) {
                                    g.setColor(Color.red);
                                    g.fillRect(100 + i * 30, 48 + j * 48, 30, 48);
                                    g.setColor(Color.blue);
                                }
                                if (i % 16 == 0 && i != 0) {
                                    g.setColor(Color.green);
                                    g.fillRect(100 + i * 30, 48 + j * 48, 30, 48);
                                    g.setColor(Color.blue);

                                }

                                g.drawRect(100 + i * 30, 48 + j * 48, 30, 48);

                            }
                        }
                        //vykresleni notes
                        for (int i = 0; i < notes.size(); i++) {
                            g.setColor(new Color(102, 102, 102));
                            g.fillRect(100 + 60 * notes.get(i).x + 2, notes.get(i).y * 24 + 192, notes.get(i).length * 60, 26);

                        }
                        //vyznaceni prvniho bloku
                        for (int i = 0; i < notes.size(); i++) {
                            g.setColor(Color.BLACK);
                            g.fillRect(100 + 60 * notes.get(i).x + 2, notes.get(i).y * 24 + 192, 3, 26);

                        }
                        
                        //obnovovani tlacitek kick, hihat, snare
                        
                        frame.repaint();

                    }

                };

            }
        };

        SwingUtilities.invokeLater(run);

        share.mousePressed = false;
        //vytvaření tlačitek
        

        startB = new JButton("Play");
        //vytvaření boolean řetězcu
        kick = new Boolean[32];
        hihat = new Boolean[32];
        snare = new Boolean[32];
        for (int i = 0; i < 32; i++) {
            kick[i] = false;
            hihat[i] = false;
            snare[i] = false;
        }

        

        for (int i = 0; i < 32; i++) {

            //kick
            int j = i;
            kickB[i] = new MyButton();
            kickB[i].setBackground(myDefaultColor);
            kickB[i].setBounds(100 + i * 30, 48, 30, 48);
            kickB[i].setVisible(true);
            kickB[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    //switch
                    kick[j] = !kick[j];

                    if (kick[j]) {
                        kickB[j].setBackground(myActiveColor);
                    } else {
                        kickB[j].setBackground(myDefaultColor);
                    }
                }
            });
            frame.add(kickB[i]);

            //hihat
            hihatB[i] = new MyButton();
            hihatB[i].setBounds(100 + i * 30, 96, 30, 48);
            hihatB[i].setVisible(true);
            hihatB[i].setBackground(myDefaultColor);
            hihatB[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hihat[j] = !hihat[j];

                    if (hihat[j]) {
                        hihatB[j].setBackground(myActiveColor);
                    } else {
                        hihatB[j].setBackground(myDefaultColor);
                    }
                }
            });
            frame.add(hihatB[i]);
            //snare
            snareB[i] = new MyButton();
            snareB[i].setBounds(100 + i * 30, 96 + 48, 30, 48);
            snareB[i].setVisible(true);
            snareB[i].setBackground(myDefaultColor);
            snareB[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    snare[j] = !snare[j];

                    if (snare[j]) {
                        snareB[j].setBackground(myActiveColor);
                    } else {
                        snareB[j].setBackground(myDefaultColor);
                    }
                }
            });
            frame.add(snareB[i]);

        }

        startB.setBounds(910, 5, 90, 120);
        startB.setVisible(true);
        startB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    playAudio();
                } catch (InterruptedException ex) {
                }

            }
        });
        saveB= new JButton();
        saveB.setBounds(1030, 5, 30, 120);
        saveB.setVisible(true);
        saveB.setBackground(Color.GREEN);
        saveB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    saveAudio();
                } catch (Exception ex) {
                }
            }
        });
        upB= new JButton();
        upB.setBounds(1000, 5, 30, 120);
        upB.setVisible(true);
        upB.setBackground(Color.RED);
        upB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    uploadAudio();
                } catch (Exception ex) {
                }
            }
        });
        
        frame.add(upB);
        frame.add(saveB);
        frame.add(startB);
        panel.addMouseListener(mou);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

//        SineOscillator sineOsc = new SineOscillator();
//        LineOut  lineOut = new LineOut();
//        sineOsc.output.connect( 0, lineOut.input, 0 );   // connect to left channel
//        sineOsc.output.connect( 0, lineOut.input, 1 );   // connect to right channel
//        lineOut.start();
        while (RUN) {

            //adding notes
            synchronized (share) {
                if (share.mousePressed && share.MBleft) {

                    share.mouseReleased = false;
                    int mouseX;
                    int mouseY;

                    mouseX = MouseInfo.getPointerInfo().getLocation().x - frame.getLocationOnScreen().x;
                    mouseY = MouseInfo.getPointerInfo().getLocation().y - frame.getLocationOnScreen().y;

                    int x = Math.round((mouseX - 100) / 60);
                    int y = Math.round((mouseY - 216) / 24);
                    
                    //kontrola ze neklikas do stejneho mista 2krat
                    if (!(x < 0 || y < 0) && x < 16 && y < 25) {
                        boolean isUniqueNote = true;
                        for (int i = 0; i < notes.size(); i++) {

                            if (x == notes.get(i).x && y == notes.get(i).y || x == notes.get(i).x + notes.get(i).length - 1 && y == notes.get(i).y) {
                                isUniqueNote = false;
                            }
                        }
                        if (isUniqueNote) {
                            notes.add(new Note(x, y));
                            if (notes.get(notes.size() - 1).x + notes.get(notes.size() - 1).length - 1 > 15) {
                                notes.get(notes.size() - 1).length = 16 - notes.get(notes.size() - 1).x;
                                share.lastLength = 16 - notes.get(notes.size() - 1).x;
                            }

                        }
                        //zmena delky noty
                        mouseX = MouseInfo.getPointerInfo().getLocation().x - frame.getLocationOnScreen().x;
                        mouseY = MouseInfo.getPointerInfo().getLocation().y - frame.getLocationOnScreen().y;

                        x = Math.round((mouseX - 100) / 60);
                        y = Math.round((mouseY - 216) / 24);
                        for (int i = 0; i < notes.size(); i++) {

                            if (x == notes.get(i).x + notes.get(i).length - 1 && y == notes.get(i).y) {

                                int newX = 0;
                                while (!(share.mouseReleased)) {
                                    newX = Math.round((MouseInfo.getPointerInfo().getLocation().x - frame.getLocationOnScreen().x - 100) / 60);
                                    if (newX - notes.get(i).x + 1 < 1) {
                                        share.lastLength = 1;
                                        notes.get(i).length = 1;
                                    } else if (notes.get(i).x + newX - notes.get(i).x + 1 > 16) {
                                        notes.get(i).length = 16 - notes.get(i).x;
                                        share.lastLength = 16 - notes.get(i).x;
                                    } else {
                                        notes.get(i).length = newX - notes.get(i).x + 1;
                                        share.lastLength = newX - notes.get(i).x + 1;
                                    }
                                }

                            }
                        }

                    }

                    share.mousePressed = false;
                    share.mouseReleased = true;
                }
                if (share.mousePressed && !(share.MBleft)) {
                    int mouseX;
                    int mouseY;

                    mouseX = MouseInfo.getPointerInfo().getLocation().x - frame.getLocationOnScreen().x;
                    mouseY = MouseInfo.getPointerInfo().getLocation().y - frame.getLocationOnScreen().y;
                    int x = Math.round((mouseX - 100) / 60);
                    int y = Math.round((mouseY - 216) / 24);
                    int indexOfDelete = 0;
                    boolean remove = false;

                    LOOP:
                    for (int i = 0; i < notes.size(); i++) {
                        for (int j = notes.get(i).x; j < notes.get(i).x + notes.get(i).length; j++) {
                            if (x == j && notes.get(i).y == y) {

                                indexOfDelete = i;
                                remove = true;
                                break LOOP;
                            }
                        }

                    }
                    if (remove) {
                        notes.remove(indexOfDelete);
                        remove = false;
                    }

                    
                    share.mousePressed = false;

                }

            }
        }

    }

    

    public void playAudio() throws InterruptedException {
        startB.setEnabled(false);
        // 120 BPM
        // 1 obdelnik 0.5 sekundy
        //jdeme po radcich

        for (int i = 0; i < 24; i++) {
            //1 radek
            for (int j = 0; j < notes.size(); j++) {
                if (notes.get(j).y == i) {
                    //zapiseme notu do hudby
                    TimeStamp timeStamp = synth.createTimeStamp();
                    TimeStamp startTime = timeStamp.makeRelative(notes.get(j).x / 2.0);
                    if (((notes.get(j).x + notes.get(j).length) / 2.0) > longestStamp) {
                        longestStamp = ((notes.get(j).x + notes.get(j).length) / 2.0);
                    }
                    TimeStamp stopTime = timeStamp.makeRelative(-0.2 + (notes.get(j).x + notes.get(j).length) / 2.0);
                    lineOut.get(i).start(startTime);
                    lineOut.get(i).stop(stopTime);
                }
            }
        }

        //beats
        //kick
        samplePlayerKick.rate.set(kickSample.getFrameRate());
        for (int i = 0; i < kick.length; i++) {
            if (kick[i]) {
                TimeStamp timeStamp = synth.createTimeStamp();
                TimeStamp startTime = timeStamp.makeRelative(i / 4.0);
                if ((i / 4.0) > longestStamp) {
                    longestStamp = i / 4.0;
                }
                samplePlayerKick.dataQueue.queue(kickSample, 0, kickSample.getNumFrames(), startTime);
            }
        }

        //hihat
        samplePlayerHihat.rate.set(hihatSample.getFrameRate());
        for (int i = 0; i < hihat.length; i++) {
            if (hihat[i]) {
                TimeStamp timeStamp = synth.createTimeStamp();
                TimeStamp startTime = timeStamp.makeRelative(i / 4.0);
                if ((i / 4.0) > longestStamp) {
                    longestStamp = i / 4.0;
                }
                samplePlayerHihat.dataQueue.queue(hihatSample, 0, hihatSample.getNumFrames(), startTime);
            }
        }
        //snare
        samplePlayerSnare.rate.set(snareSample.getFrameRate());
        for (int i = 0; i < snare.length; i++) {
            if (snare[i]) {
                TimeStamp timeStamp = synth.createTimeStamp();
                TimeStamp startTime = timeStamp.makeRelative(i / 4.0);
                if ((i / 4.0) > longestStamp) {
                    longestStamp = i / 4.0;
                }
                samplePlayerSnare.dataQueue.queue(snareSample, 0, snareSample.getNumFrames(), startTime);
            }
        }

        synth.start();
        lineOutHihat.start();
        lineOutKick.start();
        lineOutSnare.start();

        synth.sleepFor(longestStamp);
        synth.stop();
        lineOutHihat.stop();
        lineOutKick.stop();
        lineOutSnare.stop();
        startB.setEnabled(true);
        samplePlayerKick.dataQueue.clear();
        samplePlayerHihat.dataQueue.clear();
        samplePlayerSnare.dataQueue.clear();
    }

    public void saveAudio() throws FileNotFoundException, InterruptedException, IOException {
        
        
     
        saveWindow = new JFrame();
        saveWindow.getContentPane().setPreferredSize(new Dimension(200,50));
        
        saveWindow.pack();
        saveWindow.setTitle("Pianomix");
        saveWindow.setVisible(true);
        saveWindow.setResizable(false);
        
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout());
        
        textField = new JTextField(10);
        textField.setVisible(true);
        
        JButton submitB = new JButton("Submit");
        submitB.setVisible(true);
        submitB.setSize(30, 30);
        submitB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        createSaveFile(textField.getText());
                    } catch (IOException ex) {
                       
                    }
                    saveWindow.setVisible(false);
                }
            });
        
        savePanel.add(submitB);
        savePanel.add(textField);
        saveWindow.add(savePanel);
        //format  ArrList(){Boolean kick[],Boolean hihat[],Boolean snare[],ArrayList notes}
    
    }
    public void uploadAudio() throws FileNotFoundException, InterruptedException, IOException, ClassNotFoundException {

        JFileChooser fileChooser = new JFileChooser();
        int response = fileChooser.showOpenDialog(null);
        
        if(response ==JFileChooser.APPROVE_OPTION ){
            FileInputStream f = new FileInputStream(fileChooser.getSelectedFile().getAbsolutePath());
            ObjectInputStream in = new ObjectInputStream(f);
            kick = (Boolean[]) in.readObject();
            hihat = (Boolean[]) in.readObject();
            snare = (Boolean[]) in.readObject();
            int[] noteX =  (int[]) in.readObject();
            int[] noteY =  (int[]) in.readObject();
            int[] noteLenght =  (int[]) in.readObject();
            f.close();
            in.close();
            
            notes.clear();
            
            for (int i = 0; i < 400; i++) {
            if(noteX[i]!=-1){
                notes.add(new Note(noteX[i],noteY[i],noteLenght[i]));
            }else{
                break;
            }
        }
        }
        

        
    // obnovovani tlacitek
        for (int i = 0; i < 32; i++) {
            if(kick[i]==true){
                kickB[i].getModel().setSelected(true);
                kickB[i].setBackground(myActiveColor);
            }    
            else{
                kickB[i].getModel().setSelected(false);
                kickB[i].setBackground(myDefaultColor);
            }
            
            if(hihat[i]==true){
                hihatB[i].getModel().setSelected(true);
                hihatB[i].setBackground(myActiveColor);
            }    
            else{
                hihatB[i].getModel().setSelected(false);
                hihatB[i].setBackground(myDefaultColor);
            }
            
            if(snare[i]==true){
                snareB[i].getModel().setSelected(true);
                snareB[i].setBackground(myActiveColor);
            }    
            else{
                snareB[i].getModel().setSelected(false);
                snareB[i].setBackground(myDefaultColor);
            }
            
                kickB[i].repaint();
                hihatB[i].repaint();
                snareB[i].repaint();
            }
        
        
    }
    public void createSaveFile(String name) throws IOException{
        
        
            
//        ArrayList save = new ArrayList();
//        save.add(kick);
//        save.add(hihat);
//        save.add(snare);
//        save.add(notes);
//        System.out.println(save.getClass());
//        
//        FileWriter f = new FileWriter(name+".json");
//        f.write(gson.toJson(save));
//        f.close();
        FileOutputStream f = new FileOutputStream(name+".pmx");
        ObjectOutputStream out = new ObjectOutputStream(f);
        out.writeObject(kick);
        out.writeObject(hihat);
        out.writeObject(snare);
        
//        out.writeObject(notes);
        int[] noteX = new int[400];
        int[] noteY = new int[400];
        int[] noteLength = new int[400];
        for (int i = 0; i < 400; i++) {
            
            if(notes.size()>i){
            noteX[i]=notes.get(i).x;
            noteY[i]=notes.get(i).y;
            noteLength[i]=notes.get(i).length;
            }
            else{
                noteX[i]=-1;
            noteY[i]=-1;
            noteLength[i]=-1;
            }
        }
        out.writeObject(noteX);
        out.writeObject(noteY);
        out.writeObject(noteLength);
        f.close();
        out.close();


    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        PianoMix p = new PianoMix();

    }

}

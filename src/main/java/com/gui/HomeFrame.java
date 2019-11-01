/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gui;

import com.classes.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.classes.Sistema;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import com.classes.Funcoes;
import com.pi4j.io.gpio.*;
import static java.lang.Thread.sleep;
import java.util.HashMap;

/**
 *
 * @author tfaugusto
 */
public final class HomeFrame extends javax.swing.JFrame {

    //Declarar sistema
    private final Sistema sistema;

    //definir varíaveis
    private static final PinState pinStateOn = PinState.HIGH;
    private static final PinState pinStateOff = PinState.LOW;
    private static final Pin relayVentInv = RaspiPin.GPIO_00;
    private static final String relayVentInvName = "Relay Inverno";
    private static final Pin relayVentVer = RaspiPin.GPIO_01;
    private static final String relayVentVerName = "Relay Verão";
    private static final Pin relayValvulaA = RaspiPin.GPIO_02;
    private static final String relayValvulaAName = "Relay Valvula A";
    private static final Pin relayValvulaB = RaspiPin.GPIO_03;
    private static final String relayValvulaBName = "Relay Valvula B";
    private static final Pin relaySensores = RaspiPin.GPIO_04;
    private static final String relaySensoresName = "Relay Sensores";
    private static final Pin relayVentoinhaCPU = RaspiPin.GPIO_05;
    private static final String relayVentoinhaCPUName = "Relay Ventoinha CPU";
    

    //volatile variables
    public volatile HashMap<String, Float> tempHumid;
    public volatile Integer temperaturaDefinida;
    public volatile Float temperaturaAmbiente;
    public volatile Float humidadeAmbiente;
    public volatile Double temperaturaArNovo;
    public volatile Double temperaturaArInsuflacao;
    public volatile Double temperaturaArRetorno;

    public volatile boolean flagInvernoLigar;
    public volatile boolean flagInvernoDesligar;
    public volatile boolean flagVeraoLigar;
    public volatile boolean flagVeraoDesligar;

    final GpioController gpio = GpioFactory.getInstance();
    //provision gpio pin as an output pin and turn on
    final GpioPinDigitalOutput relay1 = gpio.provisionDigitalOutputPin(relayVentInv, relayVentInvName, pinStateOff);
    final GpioPinDigitalOutput relay2 = gpio.provisionDigitalOutputPin(relayVentVer, relayVentVerName, pinStateOff);
    final GpioPinDigitalOutput relay3 = gpio.provisionDigitalOutputPin(relayValvulaA, relayValvulaAName, pinStateOff);
    final GpioPinDigitalOutput relay4 = gpio.provisionDigitalOutputPin(relayValvulaB, relayValvulaBName, pinStateOff);
    final GpioPinDigitalOutput relaySensor = gpio.provisionDigitalOutputPin(relaySensores, relaySensoresName, pinStateOff);
    final GpioPinDigitalOutput relayVentoinha = gpio.provisionDigitalOutputPin(relayVentoinhaCPU, relayVentoinhaCPUName, pinStateOff);
    
    public HomeFrame(Sistema s) throws InterruptedException, IOException, ParseException {

        this.sistema = s;
        //temperatura inicial definida
        this.temperaturaDefinida = 20;
        this.temperaturaAmbiente = 0.0f;
        this.humidadeAmbiente = 0.0f;
        this.temperaturaArNovo = 0.0;
        this.temperaturaArInsuflacao = 0.0;
        this.temperaturaArRetorno = 0.0;

        initComponents();
        clock();
        getTempRaspi(2000);
        //milisec*sec*min
        restart_sensores(1000*60*60);
        
        
        //temporario();

        //TESTES RELAY
//        System.out.println("Relay 1 - relay Inverno");
//        relay1.high();
//        sleep(2000);
//        System.out.println("Relay 2 - relay Verao");
//        relay2.high();
//        sleep(2000);
//        System.out.println("Relay 3 - relay Valvula A");
//        relay3.high();
//        sleep(2000);
//        System.out.println("Relay 4 - relay Valvula A");
//        relay4.high();
//        sleep(2000);

        System.out.println("Relay Corte");
        relaySensor.low();
        sleep(2000);
        
        System.out.println("Relay Ventoinha");
        relayVentoinha.low();
        sleep(2000);
        
        
        jToggleButtonOff.doClick();

        jLabelTemperaturaDefinida.setText(Integer.toString(temperaturaDefinida) + " ºC");

        getTempHumidade(2000);
        wiredSensorTemps("28-020192453134", 0.0, 2000, "ARNOVO");
        wiredSensorTemps("28-03129779f399", 2.6, 2000, "ARINSUFLACAO");
        wiredSensorTemps("28-031597793897", 1.9, 2000, "RETORNO");
        modoInverno();
        modoVerao();

    }

    public void clock() {

        Thread clock = new Thread() {
            public void run() {
                try {
                    while (true) {
                        Calendar cal = new GregorianCalendar();
                        String day = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');
                        String month = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.MONTH)+1), 2, '0');
                        String year = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.YEAR)), 4, '0');

                        String second = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.SECOND)), 2, '0');
                        String minute = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.MINUTE)), 2, '0');
                        String hour = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.HOUR_OF_DAY)), 2, '0');

                        String dateString = year + "/" + month + "/" + day + "  " + hour + ":" + minute + ":" + second;

                        jLabelClock.setText(dateString);
                        sleep(1000);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        clock.start();
    }

    public void temporario() {

        Thread temporario = new Thread() {
            public void run() {
                try {
                    while (true) {
                        System.out.println("Numero de threads ativas: " + Thread.activeCount());
                        sleep(3000);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        temporario.start();
    }

    public void modoInverno() {

        Thread modoInverno = new Thread() {
            public void run() {
                try {
                    temperaturaAmbiente = 0.0f;
                    flagInvernoLigar = false;
                    flagInvernoDesligar = false;
                    while (true) {

                        //Se o botão estiver selecionado
                        if (jToggleButtonInverno.isSelected()) {
                            temperaturaAmbiente = tempHumid.get("Temperatura");

                            if (temperaturaAmbiente <= temperaturaDefinida) {
                                System.out.println(temperaturaAmbiente + " <= " + temperaturaDefinida);
                                System.out.println("Está o inverno ativo");

                                //Não mudar relay se já estiverem nas posiçoes certas e a temperatura tiver diferença de 1 grau
                                //FALTA FAZER CONDIÇAO da diferença de 1 grau
                                if (!flagInvernoLigar) {
                                    //Relay Inverno Desligado
                                    relay1.low();
                                    //Relay Verão Desligado
                                    relay2.low();
                                    //Abrir a valvula A e esperar durante X segundos para poder desligar
                                    relay3.high();
                                    Thread.sleep(8000);
                                    relay3.low();
                                    //Valvula B a OFF
                                    relay4.low();
                                }
                                //ativar
                                flagInvernoLigar = true;
                                //fica disponivel para voltar a ligar caso a temperatura mude
                                flagInvernoDesligar = false;

                            } else {
                                System.out.println("Nao abrir relays de Inverno");

                                if (!flagInvernoDesligar) {
                                    //Relay Inverno Ligado
                                    relay1.high();
                                    //Relay Verão Desligado
                                    relay2.low();
                                    //Valvula A OFF
                                    relay3.low();
                                    //Abrir a valvula B e esperar durante X segundos para poder desligar
                                    relay4.high();
                                    Thread.sleep(8000);
                                    relay4.low();
                                }
                                flagInvernoDesligar = true;
                                //fica disponivel para voltar a ligar caso a temperatura mude
                                flagInvernoLigar = false;
                            }

                        }

                        sleep(4000);

                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        modoInverno.start();
    }

    public void modoVerao() {

        Thread modoVerao = new Thread() {
            public void run() {
                try {
                    temperaturaAmbiente = 0.0f;
                    flagVeraoLigar = false;
                    flagVeraoDesligar = false;
                    while (true) {

                        //Se o botão estiver selecionado
                        if (jToggleButtonVerao.isSelected()) {

                            if (temperaturaArNovo <= temperaturaDefinida) {
                                System.out.println(temperaturaArNovo + " <= " + temperaturaDefinida);
                                System.out.println("Está o verao ativo");

                                //Não mudar relay se já estiverem nas posiçoes certas e a temperatura tiver diferença de 1 grau
                                //FALTA FAZER CONDIÇAO da diferença de 1 grau
                                if (!flagVeraoLigar) {
                                    //Relay Inverno Desligado
                                    relay1.low();
                                    //Relay Verão Ligado
                                    relay2.high();
                                    //Valvula A a OFF
                                    relay3.low();
                                    //Abrir a valvula A e esperar durante X segundos para poder desligar
                                    relay4.high();
                                    Thread.sleep(8000);
                                    relay4.low();

                                }
                                //ativar
                                flagVeraoLigar = true;
                                flagVeraoDesligar = false;

                            } else {
                                System.out.println("Nao abrir relays de Verao");

                                if (!flagVeraoDesligar) {
                                    //Relay Inverno Desligado
                                    relay1.low();
                                    //Relay Verão Desligado
                                    relay2.low();
                                    //Valvula A OFF
                                    relay3.low();
                                    //Valvula B OFF
                                    relay4.low();
                                }
                                flagVeraoLigar = false;
                                flagVeraoDesligar = true;
                            }
                        }
                        sleep(4000);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        modoVerao.start();
    }

    public void getTempHumidade(Integer sleepTime) {

        Thread tempHum = new Thread() {
            public void run() {
                try {
                    final DHT11 dht = new DHT11();

                    while (true) {

                        tempHumid = dht.getTemperature(29);
                        //0 - Temp // 1 - Humidade
                        jLabelAmbienteValor.setText(Float.toString(tempHumid.get("Temperatura")) + " ºC");
                        jLabelHumidadeValor.setText(Float.toString(tempHumid.get("Humidade")) + " %");
                        sleep(sleepTime);

                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        tempHum.start();
    }

    public void wiredSensorTemps(String wiredDevices, Double valor, Integer sleepTimeMilis, String option) {

        Thread wiredSensorTemps = new Thread() {
            public void run() {

                while (true) {
                    try {

                        if (option.equals("ARNOVO")) {
                            temperaturaArNovo = DS18B20.getSensorsTemperatureAdjust(wiredDevices, valor);
                            jLabelArNovoValor.setText(Double.toString(temperaturaArNovo) + " ºC");
                        } else if (option.equals("ARINSUFLACAO")) {
                            temperaturaArInsuflacao = DS18B20.getSensorsTemperatureAdjust(wiredDevices, valor);
                            jLabelArInsuflacaoValor.setText(Double.toString(temperaturaArInsuflacao) + " ºC");
                        } else if (option.equals("RETORNO")) {
                            temperaturaArRetorno = DS18B20.getSensorsTemperatureAdjust(wiredDevices, valor);
                            jLabelRetornoValor.setText(Double.toString(temperaturaArRetorno) + " ºC");
                        }

                        sleep(sleepTimeMilis);

                    } catch (InterruptedException ex) {
                        Logger.getLogger(HomeFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }

        };

        wiredSensorTemps.start();

    }

    ;
    
    public void incrementarTemp(String operacao) {

        Thread incrementarTemp = new Thread() {
            public void run() {

                if (operacao.equals("+")) {
                    temperaturaDefinida++;
                } else {
                    temperaturaDefinida--;

                }
                jLabelTemperaturaDefinida.setText(Integer.toString(temperaturaDefinida) + " ºC");
            }
        };
        incrementarTemp.start();
    }

    ;
    
    
    public void getTempRaspi(Integer sleepTime) {

        Thread tempRaspi = new Thread() {
            public void run() {
                try {
                    while (true) {

                        jLabelCPUTemp.setText("CPU: " + Sistema.getSystemTemp() + " ºC");
                        sleep(sleepTime);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | ParseException ex) {
                    Logger.getLogger(HomeFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        tempRaspi.start();
    }

    public void restart_sensores(Integer time) {

        Thread restart_sensores = new Thread() {
            public void run() {
                try {
                    while (true) {
                        
                        relaySensor.low();
                        relaySensor.high();
                        
                        sleep(4000);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        restart_sensores.start();
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelMenu = new javax.swing.JPanel();
        jLabelClock = new javax.swing.JLabel();
        jLabelDataHora = new javax.swing.JLabel();
        jLabelCPUTemp = new javax.swing.JLabel();
        jPanelSensors = new javax.swing.JPanel();
        jLabelHumidade = new javax.swing.JLabel();
        jLabelHumidadeValor = new javax.swing.JLabel();
        jLabelArInsuflacao = new javax.swing.JLabel();
        jLabelArInsuflacaoValor = new javax.swing.JLabel();
        jLabelRetorno = new javax.swing.JLabel();
        jLabelRetornoValor = new javax.swing.JLabel();
        jLabelDefineTemp = new javax.swing.JLabel();
        jButtonAumentaTemp = new javax.swing.JButton();
        jLabelTemperaturaDefinida = new javax.swing.JLabel();
        jButtonDiminuiTemp = new javax.swing.JButton();
        jLabelArNovoValor = new javax.swing.JLabel();
        jLabelArNovo = new javax.swing.JLabel();
        jLabelAmbienteValor = new javax.swing.JLabel();
        jLabelAmbiente = new javax.swing.JLabel();
        jPanelControl = new javax.swing.JPanel();
        jToggleButtonManual = new javax.swing.JToggleButton();
        jToggleButtonOff = new javax.swing.JToggleButton();
        jToggleButtonVerao = new javax.swing.JToggleButton();
        jToggleButtonInverno = new javax.swing.JToggleButton();
        jPanelPorteiro = new javax.swing.JPanel();
        jButtonSair = new javax.swing.JButton();
        jButtonDesligar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMaximumSize(new java.awt.Dimension(800, 480));
        setMinimumSize(new java.awt.Dimension(800, 480));
        setName("JFrameHome"); // NOI18N
        setPreferredSize(new java.awt.Dimension(800, 480));
        setResizable(false);
        setSize(new java.awt.Dimension(800, 480));

        jPanelMenu.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Moradia Augusto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14))); // NOI18N

        jLabelClock.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelClock.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelClock.setText("Clock");

        jLabelDataHora.setFont(new java.awt.Font("Arial", 3, 14)); // NOI18N
        jLabelDataHora.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelDataHora.setText("Data/Hora:");

        jLabelCPUTemp.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelCPUTemp.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelCPUTemp.setText("PiTemp");

        javax.swing.GroupLayout jPanelMenuLayout = new javax.swing.GroupLayout(jPanelMenu);
        jPanelMenu.setLayout(jPanelMenuLayout);
        jPanelMenuLayout.setHorizontalGroup(
            jPanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelCPUTemp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                .addComponent(jLabelDataHora)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelClock, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelMenuLayout.setVerticalGroup(
            jPanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabelClock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelDataHora)
                .addComponent(jLabelCPUTemp))
        );

        jPanelSensors.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sensores", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14))); // NOI18N
        jPanelSensors.setMaximumSize(new java.awt.Dimension(800, 480));
        jPanelSensors.setPreferredSize(new java.awt.Dimension(800, 480));

        jLabelHumidade.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelHumidade.setText("Humidade:");

        jLabelHumidadeValor.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelHumidadeValor.setText("N/A");

        jLabelArInsuflacao.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelArInsuflacao.setText("Ar Insuflação:");

        jLabelArInsuflacaoValor.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelArInsuflacaoValor.setText("N/A");

        jLabelRetorno.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelRetorno.setText("Retorno:");

        jLabelRetornoValor.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelRetornoValor.setText("N/A");

        jLabelDefineTemp.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelDefineTemp.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelDefineTemp.setText("Temperatura definida:");

        jButtonAumentaTemp.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jButtonAumentaTemp.setText("+");
        jButtonAumentaTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAumentaTempActionPerformed(evt);
            }
        });

        jLabelTemperaturaDefinida.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelTemperaturaDefinida.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTemperaturaDefinida.setText("20 ºC");
        jLabelTemperaturaDefinida.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabelTemperaturaDefinida.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jButtonDiminuiTemp.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jButtonDiminuiTemp.setText("-");
        jButtonDiminuiTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDiminuiTempActionPerformed(evt);
            }
        });

        jLabelArNovoValor.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelArNovoValor.setText("N/A");

        jLabelArNovo.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelArNovo.setText("Ar Novo:");

        jLabelAmbienteValor.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabelAmbienteValor.setText("N/A");

        jLabelAmbiente.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabelAmbiente.setText("Ambiente:");

        javax.swing.GroupLayout jPanelSensorsLayout = new javax.swing.GroupLayout(jPanelSensors);
        jPanelSensors.setLayout(jPanelSensorsLayout);
        jPanelSensorsLayout.setHorizontalGroup(
            jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSensorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelSensorsLayout.createSequentialGroup()
                        .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelAmbiente, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelAmbienteValor, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelArNovoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelSensorsLayout.createSequentialGroup()
                                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelHumidadeValor, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabelArInsuflacaoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(33, Short.MAX_VALUE))
                            .addComponent(jLabelHumidade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSensorsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabelDefineTemp, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                            .addComponent(jLabelTemperaturaDefinida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelSensorsLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jButtonDiminuiTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonAumentaTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(58, 58, 58))
                    .addGroup(jPanelSensorsLayout.createSequentialGroup()
                        .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabelArNovo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelRetorno, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                            .addComponent(jLabelRetornoValor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelArInsuflacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanelSensorsLayout.setVerticalGroup(
            jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSensorsLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelAmbiente)
                    .addComponent(jLabelHumidade))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelHumidadeValor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelAmbienteValor))
                .addGap(18, 18, 18)
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelArInsuflacao, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelArNovo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelArNovoValor)
                    .addComponent(jLabelArInsuflacaoValor))
                .addGap(22, 22, 22)
                .addComponent(jLabelRetorno, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelRetornoValor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelDefineTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTemperaturaDefinida)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSensorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonDiminuiTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonAumentaTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanelControl.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Controlador", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14))); // NOI18N

        jToggleButtonManual.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jToggleButtonManual.setText("Manual");
        jToggleButtonManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonManualActionPerformed(evt);
            }
        });

        jToggleButtonOff.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jToggleButtonOff.setText("OFF");
        jToggleButtonOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonOffActionPerformed(evt);
            }
        });

        jToggleButtonVerao.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jToggleButtonVerao.setText("Verão");
        jToggleButtonVerao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonVeraoActionPerformed(evt);
            }
        });

        jToggleButtonInverno.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jToggleButtonInverno.setText("Inverno");
        jToggleButtonInverno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonInvernoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelControlLayout = new javax.swing.GroupLayout(jPanelControl);
        jPanelControl.setLayout(jPanelControlLayout);
        jPanelControlLayout.setHorizontalGroup(
            jPanelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButtonVerao, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonManual, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonOff, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonInverno, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanelControlLayout.setVerticalGroup(
            jPanelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelControlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToggleButtonOff, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jToggleButtonManual, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jToggleButtonInverno, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jToggleButtonVerao, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelPorteiro.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Porteiro", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 3, 14))); // NOI18N

        javax.swing.GroupLayout jPanelPorteiroLayout = new javax.swing.GroupLayout(jPanelPorteiro);
        jPanelPorteiro.setLayout(jPanelPorteiroLayout);
        jPanelPorteiroLayout.setHorizontalGroup(
            jPanelPorteiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 246, Short.MAX_VALUE)
        );
        jPanelPorteiroLayout.setVerticalGroup(
            jPanelPorteiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 319, Short.MAX_VALUE)
        );

        jButtonSair.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jButtonSair.setText("Sair");
        jButtonSair.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSairActionPerformed(evt);
            }
        });

        jButtonDesligar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jButtonDesligar.setText("Desligar");
        jButtonDesligar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonDesligar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDesligarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelSensors, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jPanelPorteiro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonDesligar, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSair, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonDesligar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanelSensors, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                    .addComponent(jPanelPorteiro, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelControl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(58, 58, 58))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSairActionPerformed

        System.exit(0);
    }//GEN-LAST:event_jButtonSairActionPerformed

    private void jButtonDiminuiTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDiminuiTempActionPerformed
        // TODO add your handling code here:
        incrementarTemp("-");
    }//GEN-LAST:event_jButtonDiminuiTempActionPerformed

    private void jButtonAumentaTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAumentaTempActionPerformed
        // TODO add your handling code here:
        incrementarTemp("+");
    }//GEN-LAST:event_jButtonAumentaTempActionPerformed

    private void jToggleButtonInvernoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonInvernoActionPerformed
        // ButtonActions Inverno

        if (jToggleButtonInverno.isSelected()) {
            //reset flags sempre que altera o controlador
            flagInvernoDesligar = false;
            flagInvernoLigar = false;
            flagVeraoDesligar = false;
            flagVeraoLigar = false;

            jToggleButtonOff.setSelected(false);
            jToggleButtonManual.setSelected(false);
            jToggleButtonVerao.setSelected(false);

        } else {
            jToggleButtonInverno.setSelected(true);
        }
    }//GEN-LAST:event_jToggleButtonInvernoActionPerformed

    private void jToggleButtonVeraoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonVeraoActionPerformed
        // ButtonActions Verao

        if (jToggleButtonVerao.isSelected()) {
            //reset flags sempre que altera o controlador
            flagInvernoDesligar = false;
            flagInvernoLigar = false;
            flagVeraoDesligar = false;
            flagVeraoLigar = false;

            jToggleButtonOff.setSelected(false);
            jToggleButtonManual.setSelected(false);
            jToggleButtonInverno.setSelected(false);

        } else {
            jToggleButtonVerao.setSelected(true);
        }

    }//GEN-LAST:event_jToggleButtonVeraoActionPerformed

    private void jToggleButtonOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonOffActionPerformed
        // ButtonActions Off

        if (jToggleButtonOff.isSelected()) {
            //reset flags sempre que altera o controlador
            flagInvernoDesligar = false;
            flagInvernoLigar = false;
            flagVeraoDesligar = false;
            flagVeraoLigar = false;

            Thread buttonOff = new Thread() {
                public void run() {
                    try {

                        jToggleButtonManual.setSelected(false);
                        jToggleButtonInverno.setSelected(false);
                        jToggleButtonVerao.setSelected(false);

                        //Relay Inverno Ligado
                        relay1.high();
                        //Relay Verão Desligado
                        relay2.low();
                        //Valvula A a OFF
                        relay3.low();
                        //Abrir a valvula B e esperar durante X segundos para poder desligar
                        relay4.high();

                        Thread.sleep(8000);

                        relay4.low();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            buttonOff.start();

        } else {
            jToggleButtonOff.setSelected(true);
        }
    }//GEN-LAST:event_jToggleButtonOffActionPerformed

    private void jToggleButtonManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonManualActionPerformed
        // ButtonActions Manual

        if (jToggleButtonManual.isSelected()) {
            //reset flags sempre que altera o controlador
            flagInvernoDesligar = false;
            flagInvernoLigar = false;
            flagVeraoDesligar = false;
            flagVeraoLigar = false;

            jToggleButtonOff.setSelected(false);
            jToggleButtonInverno.setSelected(false);
            jToggleButtonVerao.setSelected(false);

            //Relay Inverno Desligado
            relay1.low();
            //Relay Verão Ligado
            relay2.high();
            //Valvula A a OFF
            relay3.low();
            //Valvula B a OFF
            relay4.low();

        } else {
            jToggleButtonManual.setSelected(true);
        }
    }//GEN-LAST:event_jToggleButtonManualActionPerformed

    private void jButtonDesligarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDesligarActionPerformed
        Sistema.shutdownRaspi();
    }//GEN-LAST:event_jButtonDesligarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, IOException, ParseException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                Sistema s = new Sistema();

                try {

                    new HomeFrame(s).setVisible(true);

                } catch (InterruptedException ex) {
                    Logger.getLogger(HomeFrame.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(HomeFrame.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(HomeFrame.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAumentaTemp;
    private javax.swing.JButton jButtonDesligar;
    private javax.swing.JButton jButtonDiminuiTemp;
    private javax.swing.JButton jButtonSair;
    private javax.swing.JLabel jLabelAmbiente;
    private javax.swing.JLabel jLabelAmbienteValor;
    private javax.swing.JLabel jLabelArInsuflacao;
    private javax.swing.JLabel jLabelArInsuflacaoValor;
    private javax.swing.JLabel jLabelArNovo;
    private javax.swing.JLabel jLabelArNovoValor;
    private javax.swing.JLabel jLabelCPUTemp;
    private javax.swing.JLabel jLabelClock;
    private javax.swing.JLabel jLabelDataHora;
    private javax.swing.JLabel jLabelDefineTemp;
    private javax.swing.JLabel jLabelHumidade;
    private javax.swing.JLabel jLabelHumidadeValor;
    private javax.swing.JLabel jLabelRetorno;
    private javax.swing.JLabel jLabelRetornoValor;
    private javax.swing.JLabel jLabelTemperaturaDefinida;
    private javax.swing.JPanel jPanelControl;
    private javax.swing.JPanel jPanelMenu;
    private javax.swing.JPanel jPanelPorteiro;
    private javax.swing.JPanel jPanelSensors;
    private javax.swing.JToggleButton jToggleButtonInverno;
    private javax.swing.JToggleButton jToggleButtonManual;
    private javax.swing.JToggleButton jToggleButtonOff;
    private javax.swing.JToggleButton jToggleButtonVerao;
    // End of variables declaration//GEN-END:variables

}

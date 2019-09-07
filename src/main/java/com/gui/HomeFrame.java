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
    private static Integer temperaturaDefinida = 20;
    public volatile HashMap<String, Float> tempHumid;

    final GpioController gpio = GpioFactory.getInstance();
    //provision gpio pin as an output pin and turn on
    final GpioPinDigitalOutput relay1 = gpio.provisionDigitalOutputPin(relayVentInv, relayVentInvName, pinStateOff);
    final GpioPinDigitalOutput relay2 = gpio.provisionDigitalOutputPin(relayVentVer, relayVentVerName, pinStateOff);
    final GpioPinDigitalOutput relay3 = gpio.provisionDigitalOutputPin(relayValvulaA, relayValvulaAName, pinStateOff);
    final GpioPinDigitalOutput relay4 = gpio.provisionDigitalOutputPin(relayValvulaB, relayValvulaBName, pinStateOff);

    public HomeFrame(Sistema s) throws InterruptedException, IOException, ParseException {

        this.sistema = s;
        initComponents();
        clock();
        temporario();

        //Não está a efetuar ações -> deveria ser na main
        jToggleButtonOff.setSelected(true);

        jLabelTemperaturaDefinida.setText(Integer.toString(temperaturaDefinida) + " ºC");

        getTempHumidade(2000);
        wiredSensorTemps("28-020192453134", 0.0, 2000, "ARNOVO");
        wiredSensorTemps("28-03129779f399", 2.7, 2000, "ARINSUFLACAO");
        wiredSensorTemps("28-031597793897", 2.0, 2000, "RETORNO");

    }

    public void clock() {

        Thread clock = new Thread() {
            public void run() {
                try {
                    while (true) {
                        Calendar cal = new GregorianCalendar();
                        String day = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), 2, '0');
                        String month = Funcoes.padLeftZeros(Integer.toString(cal.get(Calendar.MONTH)), 2, '0');
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
                        System.err.println("Temperatura: " + Integer.toString(temperaturaDefinida));
                        sleep(3000);
                        System.err.println("Numero de threads ativas: " + Thread.activeCount());
                        System.err.println(Float.toString(tempHumid.get("Temperatura")) + " ºC");
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
                    while (true) {
                        System.err.println("Temperatura: " + Integer.toString(temperaturaDefinida));
                        sleep(4000);
                        System.err.println("Numero de threads ativas: " + Thread.activeCount());
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(Sistema.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        modoInverno.start();
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

                Double temp;

                while (true) {
                    try {

                        temp = DS18B20.getSensorsTemperatureAdjust(wiredDevices, valor);
                        if (option.equals("ARNOVO")) {
                            jLabelArNovoValor.setText(Double.toString(temp) + " ºC");
                        } else if (option.equals("ARINSUFLACAO")) {
                            jLabelArInsuflacaoValor.setText(Double.toString(temp) + " ºC");
                        } else if (option.equals("RETORNO")) {
                            jLabelRetornoValor.setText(Double.toString(temp) + " ºC");
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
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Tabs = new javax.swing.JTabbedPane();
        jPanelControloTemp = new javax.swing.JPanel();
        jToggleButtonManual = new javax.swing.JToggleButton();
        jToggleButtonOff = new javax.swing.JToggleButton();
        jToggleButtonVerao = new javax.swing.JToggleButton();
        jToggleButtonInverno = new javax.swing.JToggleButton();
        jLabelControl = new javax.swing.JLabel();
        jLabelTemperatura = new javax.swing.JLabel();
        jLabelArInsuflacao = new javax.swing.JLabel();
        jLabelArNovo = new javax.swing.JLabel();
        jLabelAmbiente = new javax.swing.JLabel();
        jLabelHumidade = new javax.swing.JLabel();
        jLabelArNovoValor = new javax.swing.JLabel();
        jLabelArInsuflacaoValor = new javax.swing.JLabel();
        jLabelAmbienteValor = new javax.swing.JLabel();
        jLabelHumidadeValor = new javax.swing.JLabel();
        jLabelRetorno = new javax.swing.JLabel();
        jLabelRetornoValor = new javax.swing.JLabel();
        jLabelDefineTemp = new javax.swing.JLabel();
        jButtonAumentaTemp = new javax.swing.JButton();
        jLabelTemperaturaDefinida = new javax.swing.JLabel();
        jButtonDiminuiTemp = new javax.swing.JButton();
        jPanelPorteiro = new javax.swing.JPanel();
        jLabelDataHora = new javax.swing.JLabel();
        jLabelClock = new javax.swing.JLabel();
        jLabelMoradiaAugusto = new javax.swing.JLabel();
        jButtonSair = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("JFrameHome"); // NOI18N
        setResizable(false);

        Tabs.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N

        jPanelControloTemp.setMaximumSize(new java.awt.Dimension(740, 550));
        jPanelControloTemp.setPreferredSize(new java.awt.Dimension(740, 550));

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

        jLabelControl.setFont(new java.awt.Font("Dialog", 1, 22)); // NOI18N
        jLabelControl.setText("Sensores Temperatura");

        jLabelTemperatura.setFont(new java.awt.Font("Dialog", 1, 22)); // NOI18N
        jLabelTemperatura.setText("Control");

        jLabelArInsuflacao.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelArInsuflacao.setText("Ar Insuflação:");

        jLabelArNovo.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelArNovo.setText("Ar Novo:");

        jLabelAmbiente.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelAmbiente.setText("Ambiente:");

        jLabelHumidade.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelHumidade.setText("Humidade:");

        jLabelArNovoValor.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelArNovoValor.setText("N/A");

        jLabelArInsuflacaoValor.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelArInsuflacaoValor.setText("N/A");

        jLabelAmbienteValor.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelAmbienteValor.setText("N/A");

        jLabelHumidadeValor.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelHumidadeValor.setText("N/A");

        jLabelRetorno.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelRetorno.setText("Retorno:");

        jLabelRetornoValor.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelRetornoValor.setText("N/A");

        jLabelDefineTemp.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelDefineTemp.setText("Controlo Temp");

        jButtonAumentaTemp.setText("+");
        jButtonAumentaTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAumentaTempActionPerformed(evt);
            }
        });

        jLabelTemperaturaDefinida.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabelTemperaturaDefinida.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTemperaturaDefinida.setText("20 ºC");
        jLabelTemperaturaDefinida.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jButtonDiminuiTemp.setText("-");
        jButtonDiminuiTemp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDiminuiTempActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelControloTempLayout = new javax.swing.GroupLayout(jPanelControloTemp);
        jPanelControloTemp.setLayout(jPanelControloTempLayout);
        jPanelControloTempLayout.setHorizontalGroup(
            jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelControloTempLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelControl, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelControloTempLayout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelAmbienteValor, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelArNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelArNovoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelAmbiente, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanelControloTempLayout.createSequentialGroup()
                                    .addGap(29, 29, 29)
                                    .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabelHumidade, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabelArInsuflacao, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelControloTempLayout.createSequentialGroup()
                                    .addGap(41, 41, 41)
                                    .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelArInsuflacaoValor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabelHumidadeValor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGroup(jPanelControloTempLayout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelRetornoValor, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelRetorno, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanelControloTempLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabelDefineTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanelControloTempLayout.createSequentialGroup()
                                    .addGap(40, 40, 40)
                                    .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabelTemperaturaDefinida, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanelControloTempLayout.createSequentialGroup()
                                            .addComponent(jButtonDiminuiTemp)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jButtonAumentaTemp)))
                                    .addGap(0, 0, Short.MAX_VALUE))))))
                .addGap(43, 43, Short.MAX_VALUE)
                .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButtonVerao, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonOff, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonManual, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTemperatura, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonInverno, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanelControloTempLayout.setVerticalGroup(
            jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControloTempLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTemperatura, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelControl, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanelControloTempLayout.createSequentialGroup()
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelHumidade)
                            .addComponent(jLabelAmbiente))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelAmbienteValor)
                            .addComponent(jLabelHumidadeValor))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelArNovo)
                            .addComponent(jLabelArInsuflacao, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelArNovoValor)
                            .addComponent(jLabelArInsuflacaoValor)))
                    .addGroup(jPanelControloTempLayout.createSequentialGroup()
                        .addComponent(jToggleButtonOff, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jToggleButtonManual, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)))
                .addGap(26, 26, 26)
                .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelControloTempLayout.createSequentialGroup()
                        .addComponent(jToggleButtonInverno, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(jToggleButtonVerao, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelControloTempLayout.createSequentialGroup()
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelRetorno, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelDefineTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelRetornoValor)
                            .addComponent(jLabelTemperaturaDefinida))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelControloTempLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonDiminuiTemp)
                            .addComponent(jButtonAumentaTemp)))))
        );

        Tabs.addTab("Controlador", jPanelControloTemp);

        javax.swing.GroupLayout jPanelPorteiroLayout = new javax.swing.GroupLayout(jPanelPorteiro);
        jPanelPorteiro.setLayout(jPanelPorteiroLayout);
        jPanelPorteiroLayout.setHorizontalGroup(
            jPanelPorteiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 662, Short.MAX_VALUE)
        );
        jPanelPorteiroLayout.setVerticalGroup(
            jPanelPorteiroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 441, Short.MAX_VALUE)
        );

        Tabs.addTab("Porteiro", jPanelPorteiro);

        jLabelDataHora.setFont(new java.awt.Font("Dialog", 3, 14)); // NOI18N
        jLabelDataHora.setText("Data/Hora:");

        jLabelClock.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabelClock.setText("Clock");

        jLabelMoradiaAugusto.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabelMoradiaAugusto.setText("Moradia Augusto");

        jButtonSair.setText("Sair");
        jButtonSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSairActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelDataHora)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelClock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabelMoradiaAugusto)
                        .addGap(98, 98, 98)
                        .addComponent(jButtonSair, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(Tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelMoradiaAugusto)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabelDataHora)
                                .addComponent(jLabelClock))))
                    .addComponent(jButtonSair, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
        );

        Tabs.getAccessibleContext().setAccessibleName("Tabs");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonOffActionPerformed
        // ButtonActions Off

        if (jToggleButtonOff.isSelected()) {

            Thread buttonOff = new Thread() {
                public void run() {
                    try {

                        jToggleButtonManual.setSelected(false);
                        jToggleButtonInverno.setSelected(false);
                        jToggleButtonVerao.setSelected(false);

                        //Relay Inverno Desligado
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

            jToggleButtonOff.setSelected(false);
            jToggleButtonInverno.setSelected(false);
            jToggleButtonVerao.setSelected(false);

            //Relay Inverno Desligado
            relay1.low();
            //Relay Verão Desligado
            relay2.high();
            //Valvula A a OFF
            relay3.low();
            //Valvula B a OFF
            relay4.low();

        } else {
            jToggleButtonManual.setSelected(true);
        }

    }//GEN-LAST:event_jToggleButtonManualActionPerformed

    private void jToggleButtonInvernoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonInvernoActionPerformed
        // ButtonActions Inverno

        if (jToggleButtonInverno.isSelected()) {

            jToggleButtonOff.setSelected(false);
            jToggleButtonManual.setSelected(false);
            jToggleButtonVerao.setSelected(false);

        } else {
            jToggleButtonInverno.setSelected(true);
        }
    }//GEN-LAST:event_jToggleButtonInvernoActionPerformed

    private void jToggleButtonVeraoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonVeraoActionPerformed
        // ButtonActions Verao

        if (jToggleButtonInverno.isSelected()) {

            jToggleButtonOff.setSelected(false);
            jToggleButtonManual.setSelected(false);
            jToggleButtonInverno.setSelected(false);

        } else {
            jToggleButtonVerao.setSelected(true);
        }


    }//GEN-LAST:event_jToggleButtonVeraoActionPerformed

    private void jButtonSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSairActionPerformed
        // TODO add your handling code here:

        System.exit(0);
    }//GEN-LAST:event_jButtonSairActionPerformed

    private void jButtonAumentaTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAumentaTempActionPerformed
        // TODO add your handling code here:
        incrementarTemp("+");
    }//GEN-LAST:event_jButtonAumentaTempActionPerformed

    private void jButtonDiminuiTempActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDiminuiTempActionPerformed
        // TODO add your handling code here:
        incrementarTemp("-");

    }//GEN-LAST:event_jButtonDiminuiTempActionPerformed

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
            java.util.logging.Logger.getLogger(HomeFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                    Logger.getLogger(HomeFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(HomeFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(HomeFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane Tabs;
    private javax.swing.JButton jButtonAumentaTemp;
    private javax.swing.JButton jButtonDiminuiTemp;
    private javax.swing.JButton jButtonSair;
    private javax.swing.JLabel jLabelAmbiente;
    private javax.swing.JLabel jLabelAmbienteValor;
    private javax.swing.JLabel jLabelArInsuflacao;
    private javax.swing.JLabel jLabelArInsuflacaoValor;
    private javax.swing.JLabel jLabelArNovo;
    private javax.swing.JLabel jLabelArNovoValor;
    private javax.swing.JLabel jLabelClock;
    private javax.swing.JLabel jLabelControl;
    private javax.swing.JLabel jLabelDataHora;
    private javax.swing.JLabel jLabelDefineTemp;
    private javax.swing.JLabel jLabelHumidade;
    private javax.swing.JLabel jLabelHumidadeValor;
    private javax.swing.JLabel jLabelMoradiaAugusto;
    private javax.swing.JLabel jLabelRetorno;
    private javax.swing.JLabel jLabelRetornoValor;
    private javax.swing.JLabel jLabelTemperatura;
    private javax.swing.JLabel jLabelTemperaturaDefinida;
    private javax.swing.JPanel jPanelControloTemp;
    private javax.swing.JPanel jPanelPorteiro;
    private javax.swing.JToggleButton jToggleButtonInverno;
    private javax.swing.JToggleButton jToggleButtonManual;
    private javax.swing.JToggleButton jToggleButtonOff;
    private javax.swing.JToggleButton jToggleButtonVerao;
    // End of variables declaration//GEN-END:variables

}

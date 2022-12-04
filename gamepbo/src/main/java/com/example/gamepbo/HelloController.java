package com.example.gamepbo;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HelloController {

    @FXML public Button buttonMulai;
    @FXML public Button buttonSkor;
    @FXML public TextField boxNama;
    @FXML public ToggleGroup kesulitan;
    @FXML public AnchorPane boxCanvas;
    @FXML public Canvas canvasGame;
    @FXML public Label labelSkor;
    @FXML public Label labelLevel;
    @FXML public AnchorPane boxKonfirmasi;
    @FXML public Label labelKonfirmasi;
    @FXML public Button buttonLanjut;
    @FXML public Button buttonKeluar;
    @FXML public Button buttonHapus;
    @FXML public Label labelWaktu;
    @FXML public StackPane boxTabel;
    @FXML public Button buttonOkeSkor;
    @FXML public AnchorPane boxSkor;

    private GraphicsContext gc;
    private ArrayList<Sprite> kumpulanMusuh;
    private String kesulitanString;
    private int skor;
    private int level;
    private DBConnector db;

    static class LongValue
    {
        public long value;
        public LongValue(long i)
        {
            value = i;
        }
    }

    static class seconds extends TimerTask {
        private int seconds;
        public seconds(int seconds) {
            this.seconds = seconds;
        }
        public int getSeconds() {
            return seconds;
        }
        @Override
        public void run() { seconds--; }
    }

    public HelloController(){
        kumpulanMusuh = new ArrayList<>();
        db = new DBConnector();
        kesulitanString = "Mudah";
    }

    public void buttonMulaiClicked(ActionEvent e) {
        RadioButton rb = (RadioButton) kesulitan.getSelectedToggle();
        kesulitanString = rb.getText();
        boxCanvas.setVisible(true);
        boxCanvas.setDisable(false);
        level = 1;
        skor = 0;
        mulaiLevel(kesulitanString);
    }

    public void mulaiLevel(String k){
        if (kumpulanMusuh.size() != 0) kumpulanMusuh.removeAll(kumpulanMusuh);
        labelLevel.setText("Level " + level);
        gc = canvasGame.getGraphicsContext2D();
        Image musuhImg = new Image(String.valueOf(getClass().getResource("musuh.png")), 35, 35, true, true);
        for(int i = 0; i < level; i++){
            Sprite musuh = new Sprite();
            musuh.setImage(musuhImg);
            musuh.setVelocity(0,0);
            switch (k){
                case "Sedang" -> musuh.addVelocity(Math.random() * 100 - 50,Math.random() * 100 - 50);
                case "Sulit" -> musuh.addVelocity(Math.random() * 200 - 100,Math.random() * 200 - 100);
            }
            kumpulanMusuh.add(musuh);
        }
        gc.drawImage(musuhImg, 0,0);
        LongValue lastNanoTime = new LongValue(System.nanoTime());
        Timer timer = new Timer();
        seconds sec = new seconds(5+level*2);
        timer.scheduleAtFixedRate(sec, 0, 1000);

        new AnimationTimer () {
            @Override
            public void handle(long currentNanoTime) {

                // calculate time since last update.
                double elapsedTime = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
                lastNanoTime.value = currentNanoTime;
                labelWaktu.setText(String.format("%02d:%02d", sec.getSeconds() / 60, sec.getSeconds() % 60));
                gc.clearRect(0,0, canvasGame.getWidth(), canvasGame.getHeight());
                for(Sprite musuh : kumpulanMusuh){
                    musuh.update(elapsedTime);
                    musuh.render(gc);
                }

                // menang
                if(kumpulanMusuh.isEmpty()){
                    boxKonfirmasi.setDisable(false);
                    boxKonfirmasi.setVisible(true);
                    buttonLanjut.setDisable(false);
                    labelKonfirmasi.setText("Kamu Menang!");
                    timer.cancel();
                    stop();
                }
                // kalah
                if (sec.getSeconds() <= 0) {
                    boxKonfirmasi.setDisable(false);
                    boxKonfirmasi.setVisible(true);
                    buttonLanjut.setDisable(true);
                    labelKonfirmasi.setText("Waktu Habis!");
                    timer.cancel();
                    stop();
                }

            }
        }.start();

    }

    public void canvasOnClick(MouseEvent e){
        Point2D mousePoint = new Point2D(e.getX(), e.getY());
        for(Sprite musuh : kumpulanMusuh){
            if(musuh.getBoundary().contains(mousePoint)){
                kumpulanMusuh.remove(musuh);
                skor++;
                break;
            }
        }
        labelSkor.setText(String.valueOf(skor));
    }

    public void buttonLanjutClicked(ActionEvent e){
        boxKonfirmasi.setVisible(false);
        boxKonfirmasi.setDisable(true);
        level++;
        mulaiLevel(kesulitanString);
    }

    public void buttonKeluarClicked(ActionEvent e){
        boxKonfirmasi.setVisible(false);
        boxKonfirmasi.setDisable(true);
        boxCanvas.setVisible(false);
        boxCanvas.setDisable(true);
        kumpulanMusuh.removeAll(kumpulanMusuh);
        db.insert("INSERT INTO data VALUES ('"+boxNama.getText()+"', '"+kesulitanString+"', '"+skor+"', '"+level+"')");
    }

    public void buttonSkorClicked(ActionEvent e){
        RadioButton rb = (RadioButton) kesulitan.getSelectedToggle();
        kesulitanString = rb.getText();
        db.start("SELECT * FROM data WHERE kesulitan='"+kesulitanString+"' ORDER BY skor DESC LIMIT 10");
        boxTabel.getChildren().remove(boxTabel.getChildren().size()-1);
        boxTabel.getChildren().add(db.getTable());
        boxSkor.setDisable(false);
        boxSkor.setVisible(true);
    }

    public void buttonOkeSkorClicked(ActionEvent e){
        boxSkor.setVisible(false);
        boxSkor.setDisable(true);
    }
    public void buttonHapusClicked(ActionEvent e){
        db.insert("TRUNCATE TABLE data");
        boxSkor.setVisible(false);
        boxSkor.setDisable(true);
    }
}
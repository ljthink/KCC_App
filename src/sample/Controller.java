package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import purejavacomm.CommPortIdentifier;

import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;


public class Controller implements Initializable{

    @FXML
    private TextArea log;
    @FXML
    private Label label;
    @FXML
    private ComboBox<String> com;
    @FXML
    private Button startBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button openBtn;
    @FXML
    private RadioButton internal;
    @FXML
    private RadioButton external;

    private String chosenCom;

    final ToggleGroup group = new ToggleGroup();

    private String units;

    float frequency=200000000;

    private static volatile Controller ControllerInstance;

    public Controller(){
        ControllerInstance=this;
    }

    public static Controller getInstance(){

        if (null == ControllerInstance){

            synchronized (Controller.class){

                if (null == ControllerInstance){

                    ControllerInstance = new Controller();
                }
            }
        }
        return ControllerInstance;
    }

    public void onStartAction(ActionEvent actionEvent) {
        Port.getInstance().SendRead((byte)69);
        long impulses = Port.getInstance().readTime();
        if (impulses != 0) {
            float time = calculateTime(impulses);
            log("Estimated time is: " + time + " " + units + "\n");
        }
        else{
            log("Estimated time is 0 s\n");

        }
    }

    private float calculateTime(long impulses) {
        float time=impulses/(16*frequency);
        if (time<0.000001){
            time=time*1000000000;
            units="ns";
        }
        else if(time<0.001){
            time=time*1000000;
            units="us";
        }
        else if(time<1){
            time=time*1000;
            units="ms";
        }
        else{
            units="s";
        }
        return time;
    }

    public void onStopAction(ActionEvent actionEvent) {
        log.clear();
    }

    public void log(String text) {
        log.appendText(text);
    }

    public void onSelectCom(ActionEvent actionEvent) {
        if (null != com.getSelectionModel().getSelectedItem()){

            if (null == chosenCom)
                chosenCom = "";

            if ( !chosenCom.equals(com.getSelectionModel().getSelectedItem()) ) {
                chosenCom = com.getSelectionModel().getSelectedItem();
            }
        }
    }

    public void onListPorts(MouseEvent mouseEvent) {
        com.getItems().clear();

        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

        while(ports.hasMoreElements())
        {
            com.getItems().add(ports.nextElement().getName());
        }
    }

    public void onOpen(ActionEvent actionEvent) {
        if(openBtn.getText().equals("OPEN")){
            if(null!=com.getSelectionModel().getSelectedItem()) {
                String comPort = com.getSelectionModel().getSelectedItem();
                Port.getInstance().open(comPort);
                openBtn.setText("CLOSE");
                startBtn.setDisable(false);
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("COM port has not been chosen");
                alert.setHeaderText("To open the port you have to choose a COM port");
                alert.setContentText("Please, choose a COM port");
                alert.showAndWait();
            }
        }
        else{
                Port.getInstance().close();
                openBtn.setText("OPEN");
                startBtn.setDisable(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        internal.setToggleGroup(group);
        external.setToggleGroup(group);
        internal.setSelected(true);
        startBtn.setDisable(true);
    }

    public void onChooseMode(ActionEvent actionEvent) {
        if(!internal.isSelected()){
            Port.getInstance().SendComment(new Commend((byte)96,(byte)0));
        }
        else{
            Port.getInstance().SendComment(new Commend((byte)96,(byte)1));
        }
    }
}

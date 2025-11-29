package com.nutrient_reminder.controller;

import com.nutrient_reminder.service.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

// 누락된 import 문 및 서비스 관련 import
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import com.nutrient_reminder.service.AlarmSchedulerService;
import com.nutrient_reminder.service.AlarmSchedulerService.AlarmStatusListener;

// 💡 AlarmAddPopupController.AlarmSaveListener 인터페이스와 AlarmSchedulerService.AlarmStatusListener를 모두 구현합니다.
public class MainController implements AlarmAddPopupController.AlarmSaveListener, AlarmStatusListener {

    // 💡 서비스 인스턴스 (연동용)
    private final AlarmSchedulerService service = AlarmSchedulerService.getInstance();

    @FXML
    private Label userNameLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button mainTabButton;

    @FXML
    private Button recommendTabButton;

    @FXML
    private Button addButton;

    @FXML
    private VBox alarmListContainer;

    @FXML
    public void initialize() {

        String currentId = UserSession.getUserId();
        if (currentId != null) {
            userNameLabel.setText("'" + currentId + "' 님");
        }

        System.out.println("메인 화면이 초기화되었습니다.");

        // 💡 1. MainController를 알람 상태 변화 리스너로 등록
        service.addListener(this);

        // 테스트 용 알람을 서비스에서 임시 ID를 부여하여 생성합니다.
        // 실제로는 DB에서 불러오거나 Alarm 객체를 사용해야 합니다.
        addAlarmToUI("11월 06일", "08:00", "섭취중인 약", "오전 08 : 00", "alarm_001");
        addAlarmToUI("11월 07일", "13:00", "비타민 C", "오후 01 : 00", "alarm_002");
    }

    // 알림박스 메소드 (깔끔한 디자인 및 두 개의 버튼, ID 추가)
    // 💡 ID 매개변수 추가 (연동을 위해 필요)
    public void addAlarmToUI(String dateText, String timeText, String pillName, String subTime, String alarmId) {

        // 💡 디자인 개선: 흰색 배경, 부드러운 회색 테두리 그림자 추가
        VBox alarmBox = new VBox();
        alarmBox.setId(alarmId); // 💡 VBox에 알람 ID 설정 (상태 변경 시 검색용)
        alarmBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.0, 0, 3);");
        alarmBox.setPadding(new Insets(15, 20, 15, 20)); // 안쪽 여백
        alarmBox.setSpacing(10); // 내부 요소 간격

        // 날짜 라벨
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #999999; -fx-font-size: 14px;");

        // 시간 & 약 이름 ( 가로 배치 )
        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER_LEFT); // 왼쪽 정렬
        contentBox.setSpacing(50); // 시간과 약 이름 사이 간격

        // 시간 ( ex 8시 30분 )
        Label mainTimeLabel = new Label(timeText);
        mainTimeLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 약 이름 ( ex 비타민 c )
        Label pillLabel = new Label(pillName);
        pillLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 💡 옵션 버튼 ( ... ) 추가
        Button optionButton = new Button("···");
        optionButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 24px; -fx-cursor: hand;");
        optionButton.setOnMouseEntered(this::onHoverEnter);
        optionButton.setOnMouseExited(this::onHoverExit);
        optionButton.setPrefWidth(30.0);

        // 약 이름과 옵션 버튼 사이에 공간을 채우기 위해 Pane 추가
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS); // 나머지 공간을 모두 차지하도록 설정

        // contentBox 조립
        contentBox.getChildren().addAll(mainTimeLabel, pillLabel, spacer, optionButton);

        // ----------------------------------------------------
        // 💡 하단 버튼 HBox
        // ----------------------------------------------------
        HBox buttonBar = new HBox();
        buttonBar.setSpacing(10); // 버튼 간격
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setMaxWidth(Double.MAX_VALUE);

        // 버튼 스타일
        String defaultButtonStyle = "-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;";

        // "먹었습니다" 버튼
        Button eatenButton = new Button("먹었습니다");
        eatenButton.setPrefHeight(40.0);
        eatenButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(eatenButton, Priority.ALWAYS);

        // 스타일, 마우스 이벤트 적용
        eatenButton.setStyle(defaultButtonStyle);
        eatenButton.setOnMouseEntered(this::onAlarmButtonHoverEnter);
        eatenButton.setOnMouseExited(this::onAlarmButtonHoverExit);
        eatenButton.setOnMousePressed(this::onAlarmButtonPress);
        eatenButton.setOnMouseReleased(this::onAlarmButtonRelease);

        // "30분 뒤 다시 울림" 버튼
        Button snoozeButton = new Button("30분 뒤 다시 울림");
        snoozeButton.setPrefHeight(40.0);
        snoozeButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(snoozeButton, Priority.ALWAYS);

        // 스타일, 마우스 이벤트 적용
        snoozeButton.setStyle(defaultButtonStyle);
        snoozeButton.setOnMouseEntered(this::onAlarmButtonHoverEnter);
        snoozeButton.setOnMouseExited(this::onAlarmButtonHoverExit);
        snoozeButton.setOnMousePressed(this::onAlarmButtonPress);
        snoozeButton.setOnMouseReleased(this::onAlarmButtonRelease);

        buttonBar.getChildren().addAll(eatenButton, snoozeButton);

        // 박스 조립
        alarmBox.getChildren().addAll(dateLabel, contentBox, buttonBar);

        // 실제 화면에 추가하기
        if (alarmListContainer != null) {
            alarmListContainer.getChildren().add(alarmBox);
        } else {
            System.out.println("오류: alarmListContainer가 연결되지 않았습니다. fxml의 fx:id를 확인하세요!");
        }
    }


    // 알람 버튼 마우스 이벤트

    // 마우스가 버튼에 들어오면 색상 변경
    @FXML
    private void onAlarmButtonHoverEnter(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #567889; -fx-background-radius: 10; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        button.setScaleX(1.02);
        button.setScaleY(1.02);
    }

    // 마우스가 버튼에서 나가면 원래 색상으로 돌아오기
    @FXML
    private void onAlarmButtonHoverExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    // 버튼이 눌리면 (작아지기)
    @FXML
    private void onAlarmButtonPress(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.98);
        node.setScaleY(0.98);
        System.out.println(((Button)node).getText() + " 버튼이 눌렸습니다.");
    }

    // 버튼에서 마우스를 떼면( 원래 크기로 복귀 )
    @FXML
    private void onAlarmButtonRelease(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setStyle("-fx-background-color: #E8F5FF; -fx-background-radius: 10; -fx-text-fill: #567889; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        // 원래 크기로
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }

    // 로그아웃
    @FXML
    private void handleLogout() {
        System.out.println("로그아웃 버튼 클릭됨");
        try {
            UserSession.clear();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/login-view.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("로그인");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 영양제 추천 탭 이동
    @FXML
    private void handleRecommendTab() {
        System.out.println("영양제 추천 탭 클릭됨");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/nutrient-check.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) recommendTabButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("영양제 추천");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("영양제 추천 화면으로 이동 실패");
        }
    }

    // ++ 추가 팝업
    @FXML
    private void handleAdd() {
        System.out.println("추가(+) 버튼 클릭됨");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/nutrient_reminder/view/alarmAddPopup.fxml")
            );
            Parent root = loader.load();

            // 팝업 컨트롤러를 가져와서 MainController(this)를 리스너로 설정
            AlarmAddPopupController popupController = loader.getController();
            popupController.setAlarmSaveListener(this); // 데이터 전달 설정

            Stage popupStage = new Stage();
            popupStage.initOwner(userNameLabel.getScene().getWindow());
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.setTitle("알람 추가");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // AlarmAddPopupController.AlarmSaveListener 인터페이스 구현 (팝업 데이터 수신)
    @Override
    public void onAlarmSaved(String name, List<String> days, String time) {
        // 팝업 컨트롤러로부터 받은 데이터를 화면 표시 형식에 맞게 변환

        // (임시) 새 알람 ID 생성
        String newAlarmId = "alarm_" + (System.currentTimeMillis() % 10000);

        // 날짜 텍스트 설정: 예시: "월, 수, 금요일 (매주 반복)" 또는 "반복 없음"
        String dateText = days.isEmpty()
                ? "반복 없음"
                : String.join(", ", days) + "요일 (매주 반복)";

        // 메인 시간 텍스트 설정
        String timeTextRaw = time.replaceAll("오전|오후", "").trim();
        String timeText = timeTextRaw.replaceAll(" : ", ":");

        // 하단 바 텍스트 설정
        String subTime = time;

        // 화면에 알람 박스 추가
        addAlarmToUI(dateText, timeText, name, subTime, newAlarmId);
    }

    // 💡 AlarmSchedulerService.AlarmStatusListener 인터페이스 구현 (백그라운드 알림 상태 수신)
    @Override
    public void onAlarmStatusChanged(String alarmId, String newStatus) {
        System.out.printf("메인 컨트롤러: 알람 ID %s의 상태가 %s로 변경되었음을 수신함.\n", alarmId, newStatus);

        for (Node node : alarmListContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox alarmBox = (VBox) node;
                if (alarmId.equals(alarmBox.getId())) {
                    if ("COMPLETED".equals(newStatus)) {
                        // 완료된 알람은 리스트에서 제거
                        alarmListContainer.getChildren().remove(alarmBox);
                        System.out.println("-> 메인 화면에서 알람 ID " + alarmId + "를 완료 처리 및 제거했습니다.");
                        return;
                    } else if ("SNOOZED".equals(newStatus)) {
                        // 스누즈된 알람은 시각적으로 상태 변경 가능
                        alarmBox.setStyle(alarmBox.getStyle() + "-fx-opacity: 0.5;");
                        System.out.println("-> 메인 화면에서 알람 ID " + alarmId + "를 스누즈 상태로 표시했습니다.");
                        return;
                    }
                }
            }
        }
    }

    // 마우스 들어오면 ( 작아지기 )
    @FXML
    private void onHoverEnter(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(0.98);
        node.setScaleY(0.98);
    }

    // 마우스 나가면 ( 원래대로 )
    @FXML
    private void onHoverExit(MouseEvent event) {
        Node node = (Node) event.getSource();
        node.setScaleX(1.0);
        node.setScaleY(1.0);
    }
}
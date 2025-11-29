package com.nutrient_reminder.service;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

public class AlarmSchedulerService {

    // 인터페이스를 AlarmSchedulerService 클래스의 내부 (static public)로 정의
    public static interface AlarmStatusListener {
        void onAlarmStatusChanged(String alarmId, String newStatus);
    }

    private static AlarmSchedulerService instance;
    private List<AlarmStatusListener> listeners = new ArrayList<>();

    // 알람 데이터 저장소 역할
    private List<String> activeAlarms = new ArrayList<>(List.of("alarm_001", "alarm_002"));

    private AlarmSchedulerService() {
        // 싱글톤 패턴
    }

    public static AlarmSchedulerService getInstance() {
        if (instance == null) {
            instance = new AlarmSchedulerService();
        }
        return instance;
    }

    // 리스너 등록 메서드 (MainController가 자신을 등록함)
    public void addListener(AlarmStatusListener listener) {
        listeners.add(listener);
        System.out.println("MainController가 AlarmSchedulerService에 등록되었습니다.");
    }

    // 알람 상태 변경 요청 처리 (AlarmTriggerController에서 호출됨)
    public void updateAlarmStatus(String alarmId, String status) {
        System.out.printf("서비스: 알람 ID %s의 상태를 %s로 변경 요청받음.\n", alarmId, status);

        // 실제 DB 업데이트 로직 (생략)

        // 모든 리스너(MainController)에게 변경 사실 통보
        Platform.runLater(() -> {
            for (AlarmStatusListener listener : listeners) {
                listener.onAlarmStatusChanged(alarmId, status);
            }
        });
    }

    // (임시) 현재 활성화된 알람 목록을 MainController에 제공
    public List<String> getActiveAlarms() {
        return activeAlarms;
    }
}
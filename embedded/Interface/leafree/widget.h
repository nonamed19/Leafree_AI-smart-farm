#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>
#include <QSerialPort>
#include <QSerialPortInfo>
#include <QSocketNotifier>
#include <QFile>
#include <QtMqtt/QMqttClient>

QT_BEGIN_NAMESPACE
namespace Ui { class Widget; }
QT_END_NAMESPACE

class Widget : public QWidget
{
    Q_OBJECT

public:
    Widget(QWidget *parent = nullptr);
    ~Widget();

public slots:
    void serial_pump_transmit();
    void serial_pump_act_transmit();
    void serial_pump_req_transmit();
    void serial_date_transmit();
    void serial_sensor_transmit();
    void serial_sensor_req_transmit();
    void serial_pump_received();
    void serial_sensor_received();

    void update_target_temp(const QMqttMessage& msg);
    void update_target_humid(const QMqttMessage& msg);
    void update_target_light(const QMqttMessage& msg);
    void update_target_co2(const QMqttMessage& msg);
    void update_target_duration(const QMqttMessage& msg);
    void update_target_interval(const QMqttMessage& msg);
    void update_target_moist(const QMqttMessage& msg);
    void update_crop_image(const QMqttMessage& msg);
    void handle_control_message(const QMqttMessage& msg);
    void update_time();
    void pipe_connect();
    void handle_pipe_data();

private:
    Ui::Widget *ui;
    QMqttClient* m_client;
    QSerialPort* serial_pump;
    QSerialPort* serial_sensor;
    QList<QSerialPortInfo> serial_info;

    const QString MQTT_HOST_NAME = "leafree.kr";
    const qint32 MQTT_PORT_NUM = 1883;
    const QString MQTT_USER_NAME = "testuser";
    const QString MQTT_PASSWORD = "test";
    const QString MQTT_COMMON_TOPIC = "/leafree/1";

    const QString UART1_PORT_NAME = "ttyS0";
    const QString UART2_PORT_NAME = "/dev/ttyAMA1";
    const QString UART3_PORT_NAME = "/dev/ttyAMA2";
    const QString UART4_PORT_NAME = "/dev/ttyAMA3";
    const QString UART5_PORT_NAME = "/dev/ttyAMA4";
    const QString UART_USB_PORT_NAME = "/dev/ttyUSB0";

    const double WATER_LV_MAX = 2500.0;
    const double WATER_LV_MIN = 200.0;
    const double SOIL_MOIST_MAX = 4095.0;
    const double SOIL_MOIST_MIN = 1100.0;

    const qint32 ICON_SIZE = 45;

    int respFd;
    QSocketNotifier *notifier;
    QFile *respPipe;
    QByteArray pump_buffer;
    QByteArray sensor_buffer;
    uint8_t wflag = 0;
    uint8_t aflag = 0;

    bool serial_connect_uart(QSerialPort* const serial, const QString& portName);
};
#endif // WIDGET_H

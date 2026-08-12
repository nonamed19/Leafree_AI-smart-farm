#include "widget.h"
#include "./ui_widget.h"
#include <QMessageBox>
#include <QDate>
#include <QPixmap>
#include <QSerialPort>
#include <QSerialPortInfo>
#include <QCursor>
#include <QDialog>
#include <QTimer>
#include <QFile>
#include <QTextStream>
#include <QDateTime>
#include <QPainter>
#include <QPainterPath>
#include <QtMqtt/QMqttClient>
#include <fcntl.h>
#include "data.h"

Widget::Widget(QWidget *parent)
    : QWidget(parent)
    , ui(new Ui::Widget)
{
    ui->setupUi(this);
    QApplication::setOverrideCursor(Qt::BlankCursor);
    this->setStyleSheet("background-color: #ffffff;");

    /* display current time */
    update_time();

    QTimer* date_timer = new QTimer(this);
    connect(date_timer, &QTimer::timeout, this, &Widget::update_time);
    date_timer->start(1000);

    /* Fundamental Widget Position */
    ui->stt_wrapper->addWidget(ui->stt_btn, 0, Qt::AlignHCenter);

//    QPixmap lettuce(":/src/lettuce.jpg");
//    QPixmap scaled = lettuce.scaled(500, 500, Qt::KeepAspectRatio, Qt::SmoothTransformation);
//    QPixmap rounded(scaled.size());
//    rounded.fill(Qt::transparent);

//    QPainter painter(&rounded);
//    painter.setRenderHint(QPainter::Antialiasing);
//    QPainterPath path;
//    path.addRoundedRect(scaled.rect(), 30, 30);  // (x, y) 반지름

//    painter.setClipPath(path);
//    painter.drawPixmap(0, 0, scaled);
//    ui->crop_img->setPixmap(rounded);

    //ui->crop_img->setPixmap(lettuce.scaled(500, 500, Qt::KeepAspectRatio));

    ui->debug_widget->setVisible(false);

    QPixmap temp_icon(":/src/temperature.png");
    QPixmap humid_icon(":/src/humidity.png");
    QPixmap lux_icon(":/src/lamp.png");
    QPixmap co2_icon(":/src/co2-cloud.png");
    QPixmap lv_icon(":/src/depth.png");
    QPixmap moist_icon(":/src/skin.png");
    ui->temp_emoji->setPixmap(temp_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));
    ui->humid_emoji->setPixmap(humid_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));
    ui->lux_emoji->setPixmap(lux_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));
    ui->CO2_txt->setPixmap(co2_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));
    ui->waterlv_lbl->setPixmap(lv_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));
    ui->soilmoist_lbl->setPixmap(moist_icon.scaled(ICON_SIZE, ICON_SIZE, Qt::KeepAspectRatio));

    QIcon mic_icon(":/src/microphone.png");
    ui->stt_btn->setIcon(mic_icon);
    ui->stt_btn->setIconSize(QSize(45, 45));

    /* UI style */
    ui->sensor_widget->adjustSize();
    ui->sensor_widget->setStyleSheet(
        "#sensor_widget {"
            " background-color: transparent;"
            " border: 2px solid #999;"
            " border-radius: 12px;"
        " }"
    );

    ui->pump_widget->adjustSize();
    ui->pump_widget->setStyleSheet(
        "#pump_widget {"
            " background-color: transparent;"
            " border: 2px solid #999;"
            " border-radius: 12px;"
        " }"
    );

    ui->cropinfo_widget->adjustSize();
    ui->cropinfo_widget->setStyleSheet(
        "#cropinfo_widget {"
            " background-color: transparent;"
            " border: 2px solid #999;"
            " border-radius: 12px;"
        " }"
    );

    ui->stt_btn->setStyleSheet(
        "QPushButton {"
        "  border-radius: 35px;"          // 둥근 모서리 반경 (픽셀 단위)
        "  border: 2px solid #555;"       // 테두리 색과 두께
        "  background-color: #eee;"
        "  padding: 5px 15px;"
        "}"
        "QPushButton:hover {"
        "  background-color: #ccc;"    // 마우스 오버 시 배경색
        "}"
        "QPushButton:pressed {"
        "  background-color: #aaa;"    // 클릭 시 배경색"
        "}"
    );

    /* find and connect ports */
    serial_pump = new QSerialPort(this);
    serial_sensor = new QSerialPort(this);
    if(serial_connect_uart(serial_pump, UART1_PORT_NAME)) {
        ui->uart1_state->setText("OK");

        QTimer::singleShot(800, this, [this]() {
            QTimer* pumpTimer = new QTimer(this);
            pumpTimer->setInterval(3000);
            pumpTimer->setSingleShot(false);
            connect(pumpTimer, &QTimer::timeout, [this]() {
                serial_pump_req_transmit();
            });
            pumpTimer->start();  // 0.5초 뒤에 시작되므로 간격도 어긋나게 유지됨
        });
    }
    else {
        ui->uart1_state->setText("Failed");
    }

    if(serial_connect_uart(serial_sensor, UART_USB_PORT_NAME)) {
        ui->uart2_state->setText("OK");

        QTimer* transmitTimer = new QTimer(this);
        transmitTimer->setInterval(3000);
        transmitTimer->setSingleShot(false);

        QObject::connect(transmitTimer, &QTimer::timeout, [this]() {
            serial_date_transmit();
        });

        transmitTimer->start();

        QTimer::singleShot(400, this, [this]() {
            QTimer* transmitTimer2 = new QTimer(this);
            transmitTimer2->setInterval(3000);
            transmitTimer2->setSingleShot(false);
            connect(transmitTimer2, &QTimer::timeout, [this]() {
                serial_sensor_req_transmit();
            });
            transmitTimer2->start();  // 0.5초 뒤에 시작되므로 간격도 어긋나게 유지됨
        });
    }
    else {
        ui->uart2_state->setText("Failed");
    }

    /* signal and slots */
    connect(serial_pump, &QSerialPort::readyRead, this, &Widget::serial_pump_received);
    connect(serial_sensor, &QSerialPort::readyRead, this, &Widget::serial_sensor_received);
    connect(ui->debuginfo_chk, &QCheckBox::stateChanged, [this](int state) {
        ui->debug_widget->setVisible(state == Qt::Checked);
    });

    /* mqtt */
    m_client = new QMqttClient(this);
    m_client->setHostname(MQTT_HOST_NAME);
    m_client->setPort(MQTT_PORT_NUM);
    m_client->setUsername(MQTT_USER_NAME);
    m_client->setPassword(MQTT_PASSWORD);

    connect(m_client, &QMqttClient::connected, this, [this]() {
        qDebug() << "MQTT connected!";
        // 연결 성공 후 publish 또는 subscribe 가능

        auto event_topic = m_client->subscribe(QString(MQTT_COMMON_TOPIC + "/event/#"));
        auto sensor_topic = m_client->subscribe(QString(MQTT_COMMON_TOPIC + "/sensor/#"));
        auto topic_ctrl_data = m_client->subscribe(QString(MQTT_COMMON_TOPIC + "/control/#"));
        connect(topic_ctrl_data, &QMqttSubscription::messageReceived, this, &Widget::handle_control_message);
    });

    m_client->connectToHost();

    /* pipe connection */
    connect(ui->stt_btn, &QPushButton::clicked, this, &Widget::pipe_connect);

//    for(const QSerialPortInfo& ports : QSerialPortInfo::availablePorts()) {
//        ui->port_list->addItem(ports.portName());
//    }
}

Widget::~Widget()
{
    delete ui;
}

void Widget::update_time()
{
    QString time_txt = QDate::currentDate().toString("yyyy-MM-dd") + " " + QTime::currentTime().toString("hh:mm");
    ui->date_lbl->setText(time_txt);
}

bool Widget::serial_connect_uart(QSerialPort* const serial, const QString& portName)
{
    serial->setPortName(portName);
    serial->setBaudRate(QSerialPort::Baud115200);
    serial->setDataBits(QSerialPort::Data8);
    serial->setParity(QSerialPort::NoParity);
    serial->setStopBits(QSerialPort::OneStop);
    serial->setFlowControl(QSerialPort::NoFlowControl);

    return serial->open(QIODevice::ReadWrite);
}

void Widget::handle_control_message(const QMqttMessage& msg)
{
    const QString topic = msg.topic().name();
    const QString payloadStr = QString::fromUtf8(msg.payload());

    if (topic.endsWith("/temperature")) {
        ui->target_temp_value->setText(payloadStr);
        aflag |= 1;
    } else if (topic.endsWith("/humidity")) {
        ui->target_humid_value->setText(payloadStr);
        aflag |= (1 << 1);
    } else if (topic.endsWith("/light")) {
        ui->target_light_value->setText(payloadStr);
        aflag |= (1 << 2);
    } else if (topic.endsWith("/co2_ppm")) {
        ui->target_co2_value->setText(payloadStr);
        aflag |= (1 << 3);
    } else if (topic.endsWith("/light_duration")) {
        double value = payloadStr.toFloat();
        double result = value / 60.0;
        QString formatted = QString::number(result, 'f', 1);
        ui->target_duration_value->setText(formatted);
        aflag |= (1 << 4);

    } else if (topic.endsWith("/watering_interval")) {
        double value = payloadStr.toFloat();
        double result = value / 60.0;
        QString formatted = QString::number(result, 'f', 1);
        ui->target_interval_value->setText(formatted);
        wflag |= 1;
    } else if (topic.endsWith("/soil_moisture")) {
        ui->target_moist_value->setText(payloadStr);
        wflag |= (1 << 1);

    } else if (topic.endsWith("/crop_image")) {
        update_crop_image(msg); // 예외적으로 이미지 처리는 그대로 별도 함수 사용
    }

    if(aflag == 0b11111) {
        serial_sensor_transmit();
        aflag = 0;
    }

    if(wflag == 0b11) {
        serial_pump_transmit();
        wflag = 0;
    }
}


void Widget::serial_pump_transmit()
{
    qDebug() << "pump transmit called";
    WaterCtl_t value;
    value.cmd = 0x01;
    value.interval_h = ui->target_interval_value->text().toInt();
    uint32_t percent = ui->target_moist_value->text().toInt();
    value.target_moist = SOIL_MOIST_MAX - ((SOIL_MOIST_MAX - SOIL_MOIST_MIN) * percent / 100.0);

    quint64 written = serial_pump->write(reinterpret_cast<const char*>(&value), sizeof(WaterCtl_t));
    if(written == sizeof(WaterCtl_t)) {
        qDebug() << "pump data write successful:" << written << "bytes";
        qDebug() << "target interval: " << value.interval_h;
        qDebug() << "target moist: " << value.target_moist;
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_pump_act_transmit()
{
    qDebug() << "pump activate called";
    WaterCtl_t value;
    value.cmd = 0x03;
    quint64 written = serial_pump->write(reinterpret_cast<const char*>(&value), sizeof(WaterCtl_t));
    if(written == sizeof(WaterCtl_t)) {
        qDebug() << "pump_act write successful:" << written << "bytes";
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_pump_req_transmit()
{
    qDebug() << "pump request called";
    WaterCtl_t value;
    value.cmd = 0x04;
    serial_pump->write(reinterpret_cast<const char*>(&value), sizeof(WaterCtl_t));
    quint64 written = serial_pump->write(reinterpret_cast<const char*>(&value), sizeof(WaterCtl_t));
    if(written == sizeof(WaterCtl_t)) {
        qDebug() << "pump_req write successful:" << written << "bytes";
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_sensor_transmit()
{
    AirCtl_t value;
    value.cmd = 1;
    value.temp10 = (int)(ui->target_temp_value->text().toDouble() * 10);
    value.humidity = ui->target_humid_value->text().toInt();
    value.lux = ui->target_light_value->text().toInt();
    value.co2 = ui->target_co2_value->text().toInt();
    value.light_hours = ui->target_duration_value->text().toInt();
    qint64 written = serial_sensor->write(reinterpret_cast<const char*>(&value), sizeof(AirCtl_t));
    if (written == sizeof(AirCtl_t)) {
        qDebug() << "Write successful:" << written << "bytes";
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_sensor_req_transmit()
{
    AirCtl_t value;
    value.cmd = 4;
    qint64 written = serial_sensor->write(reinterpret_cast<const char*>(&value), sizeof(AirCtl_t));
    if (written == sizeof(AirCtl_t)) {
        qDebug() << "Sensor req transmit: Write successful:" << written << "bytes";
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_date_transmit()
{
    QDateTime now = QDateTime::currentDateTime();
    RtcSetPacket_t value;
    value.cmd = 0x02;
    value.hour = now.time().hour();
    value.minute = now.time().minute();
    value.second = now.time().second();

    value.day = now.date().day();
    value.month = now.date().month();
    value.year = now.date().year() - 2000;

    qint64 written = serial_sensor->write(reinterpret_cast<const char*>(&value), sizeof(RtcSetPacket_t));
    if (written == sizeof(RtcSetPacket_t)) {
        qDebug() << "Date transmit: Write successful:" << written << "bytes";
    } else {
        qDebug() << "Write failed or partial write:" << written << "bytes";
    }
}

void Widget::serial_pump_received()
{
    pump_buffer.append(serial_pump->readAll());

    while(pump_buffer.size() >= 8) {
        QByteArray packet = pump_buffer.left(8);
        pump_buffer.remove(0, 8);

        QString hexString = packet.toHex(' ').toUpper();  // 16진수로 변환, 공백 구분
        ui->rx_pump_lbl->setText("received: " + hexString);

        SensorData_t value;
        value.soil_adc = packet[0] | (packet[1] << 8);
        value.level_adc = packet[2] | (packet[3] << 8);
        if(value.level_adc < WATER_LV_MIN) value.level_adc = WATER_LV_MIN;
        else if(value.level_adc > WATER_LV_MAX) value.level_adc = WATER_LV_MAX;
        value.sample_tick = packet[4] | (packet[5] << 8) | (packet[6] << 16) | (packet[7] << 24);

        ui->waterlv_value->setText(QString::number(((value.level_adc - WATER_LV_MIN) * 100.0) / (WATER_LV_MAX - WATER_LV_MIN), 'f', 1));
        ui->soilmoist_value->setText(QString::number(((SOIL_MOIST_MAX - value.soil_adc) * 100.0) / (SOIL_MOIST_MAX - SOIL_MOIST_MIN), 'f', 1));

        m_client->publish(MQTT_COMMON_TOPIC + "/sensor/water_level", ui->waterlv_value->text().toUtf8());
        m_client->publish(MQTT_COMMON_TOPIC + "/sensor/soil_moisture", ui->soilmoist_value->text().toUtf8());
    }

    //pump_buffer.clear();
}

void Widget::serial_sensor_received()
{
    sensor_buffer.append(serial_sensor->readAll());

    // 7바이트 이상일 경우에만 처리
    while (sensor_buffer.size() >= 7) {
        QByteArray packet = sensor_buffer.left(7);
        sensor_buffer.remove(0, 7);

        QString hexString = packet.toHex(' ').toUpper();        colorrepository.cpp
                colorrepository.h
        ui->rx_sensor_lbl->setText("received: " + hexString);

        AirStat_t value;
        value.temp10 = static_cast<uint8_t>(packet[0]) | (static_cast<uint8_t>(packet[1]) << 8);
        value.humidity = static_cast<uint8_t>(packet[2]);
        value.lux = static_cast<uint8_t>(packet[3]) | (static_cast<uint8_t>(packet[4]) << 8);
        value.co2 = static_cast<uint8_t>(packet[5]) | (static_cast<uint8_t>(packet[6]) << 8);


        ui->temp_value->setText(QString::number((double)value.temp10 / 10, 'f', 1));
        ui->humid_value->setText(QString::number(value.humidity));
        ui->lux_value->setText(QString::number(value.lux));
        ui->CO2_value->setText(QString::number(value.co2));

        m_client->publish(MQTT_COMMON_TOPIC + "/sensor/temperature", ui->temp_value->text().toUtf8());
        m_client->publish(MQTT_COMMON_TOPIC + "/sensor/humidity", ui->humid_value->text().toUtf8());
        m_client->publish(MQTT_COMMON_TOPIC + "/sensor/light", ui->lux_value->text().toUtf8());
        m_client->publish(MQTT_COMMON_TOPIC +"/sensor/co2_ppm", ui->CO2_value->text().toUtf8());
    }

    sensor_buffer.clear();
}

void Widget::update_target_temp(const QMqttMessage& msg)
{
    ui->target_temp_value->setText(QString::fromUtf8(msg.payload()));
    serial_sensor_transmit();
}

void Widget::update_target_humid(const QMqttMessage& msg)
{
    ui->target_humid_value->setText(QString::fromUtf8(msg.payload()));
    serial_sensor_transmit();
}

void Widget::update_target_light(const QMqttMessage& msg)
{
    ui->target_light_value->setText(QString::fromUtf8(msg.payload()));
    serial_sensor_transmit();
}

void Widget::update_target_co2(const QMqttMessage& msg)
{
    ui->target_co2_value->setText(QString::fromUtf8(msg.payload()));
    serial_sensor_transmit();
}

void Widget::update_target_duration(const QMqttMessage& msg)
{
    QString payloadStr = QString::fromUtf8(msg.payload());
    double value = payloadStr.toFloat();

    double result = value / 60.0;
    QString formatted = QString::number(result, 'f', 1);
    ui->target_duration_value->setText(formatted);

    serial_sensor_transmit();
}

void Widget::update_target_interval(const QMqttMessage& msg)
{
    QString payloadStr = QString::fromUtf8(msg.payload());
    double value = payloadStr.toFloat();

    double result = value / 60.0;
    QString formatted = QString::number(result, 'f', 1);
    ui->target_interval_value->setText(formatted);

    serial_pump_transmit();
}

void Widget::update_target_moist(const QMqttMessage& msg)
{
    ui->target_moist_value->setText(QString::fromUtf8(msg.payload()));
    serial_pump_transmit();
}

void Widget::update_crop_image(const QMqttMessage& msg) {
    QByteArray imageData = QByteArray::fromBase64(msg.payload());

    QImage image;
    if (!image.loadFromData(imageData)) {
        qDebug() << "Image load failed!";
        return;
    }

    QPixmap pixmap = QPixmap::fromImage(image);
    ui->crop_img->setPixmap(pixmap.scaled(ui->crop_img->size(), Qt::KeepAspectRatio));
}

void Widget::pipe_connect() {
    qDebug() << "pipeConnect called";
    QFile cmdPipe("/tmp/stt_cmd");
    if (cmdPipe.open(QIODevice::WriteOnly)) {
        QTextStream out(&cmdPipe);
        out << "START\n";
        cmdPipe.flush();
        cmdPipe.close();
    }

    int fd = open("/tmp/stt_resp", O_RDONLY | O_NONBLOCK);
    if (fd == -1) {
        qDebug() << "Failed to open /tmp/stt_resp non-blocking";
        return;
    }

    respPipe = new QFile(this);
    respPipe->open(fd, QIODevice::ReadOnly);  // setFileDescriptor internally

    qDebug() << "respPipe opened";

    // FILE* 또는 QFile의 file descriptor 가져오기
    respFd = respPipe->handle();

    // 읽기 가능 상태 감시
    qDebug() << "start watching";
    notifier = new QSocketNotifier(respFd, QSocketNotifier::Read, this);
    connect(notifier, &QSocketNotifier::activated, this, &Widget::handle_pipe_data);
}

void Widget::handle_pipe_data() {
    qDebug() << "handlePipeData called";
    if (!respPipe || !respPipe->isOpen()) return;

    QTextStream in(respPipe);
    QString line = in.readLine();

    if (line.isEmpty()) {
        qDebug() << "읽을 데이터 없음 (EOF?) → 감시 중단";
    }
    else {
        qDebug() << "수신된 데이터:" << line;
    }

    ui->crop_name->setText(line);
    m_client->publish(MQTT_COMMON_TOPIC + "/event/sown", line.toUtf8());

    notifier->setEnabled(false);
    delete notifier;

    respPipe->close();
    delete respPipe;
}

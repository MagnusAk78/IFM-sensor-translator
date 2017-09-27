package model

case class SensorInfo(
  // Source ID and connection port, key identifier of sensor
  sourceAndPort: String,

  // Sensor vendor ID
  vendorId: Int,

  // Sensor device ID
  deviceId: Int,

  // Sensor product ID
  productId: String,

  // Sensor device serial nr (Unique but Not always present)
  deviceSerialNr: String,

  // Information string about the sensor
  info: String,

  // Unit string, the unit that the measured integer value represent (e.g. cm, %, Celcius)
  unitInteger: Option[String],

  // Unit string, the unit that the measured float value represent (e.g. cm, %, Celcius)
  unitFloat: Option[String],

  // Sometimes the sensors have output signals based on triggers, this lists these meaning of these output values
  outputSignalsInfo: Option[List[String]])
    
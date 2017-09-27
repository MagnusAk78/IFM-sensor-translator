package model

case class SensorStatus(
  // Source ID and connection port, key identifier of sensor
  sourceAndPort: String,

  // Number of stored values
  processDataCount: Int,

  // Maximum number of stored values
  maxProcessDataCount: Int,

  // Last recorded process data timestamp
  lastProcessDataTimestamp: Long)
    
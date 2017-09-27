package model

case class ProcessData(
  // Source ID and connection port, key identifier of sensor
  sourceAndPort: String,

  // Timestamp from server when data was received (not from sensor)
  timestamp: Long,

  // Measured integer value , optional
  intValue: Option[Int],

  // Measured float value, optional
  floatValue: Option[Double],

  // Sometimes the sensors have output signals based on triggers, this lists these output values
  outputSignals: Option[List[Boolean]])
    
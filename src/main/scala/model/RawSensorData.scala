package model

case class RawSensorData(
  // Source ID, name of master
  sourceId: String,

  // 0 => data, 3 => disconnected sensor, hex string
  eventType: String,

  // Input port in master, 0-8, hex string
  portNr: String,

  // 310 => IFM, hex string
  vendorId: String,

  // Sensor and mode, hex string
  deviceId: String,

  // Product ID
  productId: String,

  // Device Serial Number (Not always present)
  deviceSerialNumber: String,

  // Process data, hex string (Processed differently for each sensor)
  processData: String,

  // Timestamp, server timestamp when data was received (NOT FROM IFM-master)
  timestamp: Long)
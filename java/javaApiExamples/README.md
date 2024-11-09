Various demonstrations of how to use the Java API in your applications. Kept as simple as possible so that you can quickly apply these in your solution.

## Examples for when the API is acting as a device

These examples cover the cases where the API is acting as a device, for example an embedded Raspberry PI.

* An example showing a device that tries to connect to client connections - `DeviceWithClientConnectionExample.java`
* This code allows you to make AES encryption keys `MakeEncryptionKeyExample.java`

## Examples where the API is running remotely connecting to a device

These examples cover the API running remotely and connecting to a device.

* `ClientThatAcceptsForRemoteExample.java` an API "client" that accepts connections from a device, where the device provides the bootstrap and you can control the remote application.
* `ConnectToRemoteDeviceServerExample.java` connect to a remote device on a particular socket configuration.
* `StandaloneRs232Test.java` same as above but an RS232 example.


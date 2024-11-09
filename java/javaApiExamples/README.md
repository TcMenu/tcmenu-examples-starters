Various demonstrations of how to use the Java API in your applications. Kept as simple as possible so that you can quickly apply these in your solution.

## Examples for when the API is acting as a device

These examples cover the cases where the API is acting as a `device`, for example a Raspberry PI or other Linux system embedded into a device.

* An example showing a `device` that tries to connect to the `API` which is accepting. As a `device` we would be responsible for bootstrapping and hosting the menu items - `DeviceWithClientConnectionExample.java`
* This code allows you to make AES encryption keys that work with the other examples or elsewhere `MakeEncryptionKeyExample.java`

## Examples where the API is running remotely connecting to a device

These examples cover the API running remotely and connecting to a device.

* `ClientThatAcceptsForRemoteExample.java` an API "client" that accepts connections from a device, where the device provides the bootstrap and you can control the remote application.
* `ConnectToRemoteDeviceServerExample.java` connect to a remote device on a particular socket configuration.
* `StandaloneRs232Test.java` same as above but an RS232 example.


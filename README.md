# SoundRecord
Simple Base for a SoundRecorder based on BoundService and BroadcastReceiver this project can be used to make recorder or media player or what ever you need good start to see how to properly make it.

Tree Types Services?

- Scheduled with JobScheduler available after API 21 and Broadcast Receiver 

- Started IntentService and Service

- Bound with IBinder, Handler, Messages

Scheduled Services with JobScheduler?

- Simply Schedule the service when to run and be executed.

Started?

- Well started are pretty much starting and not finishing until they execute the task but they do not return any callback of
is going to happen they just go and do their job. You don't need anything of these simple and easy.

Bound?

- These are a diffrent one they start when you need them and they are alive with their own Thread and they are alive if there
is a component that needs them if there are no more components no need to be alive.

Broadcast Receiver?

- Yes we are using a broadcast Receiver which is very usefull and we are using a custom one instead of the builtin ones.

OnStartCommand()?

- Have to return any of these

- START_NOT_STICKY: If the system kills the service after onStartCommand() returns, 
do not recreate the service unless there are pending intents to deliver. 
This is the safest option to avoid running your service when not necessary and 
when your application can simply restart any unfinished jobs.

- START_STICKY: If the system kills the service after onStartCommand() returns, 
recreate the service and call onStartCommand(), but do not redeliver the last intent. 
Instead, the system calls onStartCommand() with a null intent unless there are pending intents to start the service. 
In that case, those intents are delivered. This is suitable for media players (or similar services) 
that are not executing commands but are running indefinitely and waiting for a job.

- START_REDELIVER_INTENT: If the system kills the service after onStartCommand() returns, 
recreate the service and call onStartCommand() with the last intent that was delivered to the service.
Any pending intents are delivered in turn. This is suitable for services that are actively performing a job 
that should be immediately resumed, such as downloading a file.

How is this happening?

- Simple example what is happening with the Service and how to properly use them there is a lot of information in Android.

- And this time the information is really good and well explained what options and how to use them properly.

- This project pretty much covers everything about them if you need something more go and check the official page.

- https://developer.android.com/guide/components/services.html

#### Author

- Niki Izvorski (nikiizvorski@gmail.com)

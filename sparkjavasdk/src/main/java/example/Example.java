package example;

import java.net.URI;

import com.ciscospark.Message;
import com.ciscospark.Spark;

public class Example {
    public static void main(String[] args) {
        System.out.println("example.Example started");
        String accessToken = "ZTM2ZmE2NDgtMzliZC00MWE1LTljMzMtM2UyYzJkNWVhNjA2ZTRhOGFhYjEtMTY1";
        System.out.println("Access token: {}" + accessToken);

        // Initialize the client
        Spark spark = Spark.builder()
                .baseUrl(URI.create("https://api.ciscospark.com/v1"))
                .accessToken(accessToken)
                .build();

        // List the rooms that I'm in
        spark.rooms()
                .iterate()
                .forEachRemaining(room -> {
                    System.out.println(room.getTitle() + ", created " + room.getCreated() + ": " + room.getId());
                });

        // Post a text message to the room
        System.out.println("");
        System.out.println("Posting a new message...");
        Message message = new Message();
        message.setRoomId("Y2lzY29zcGFyazovL3VzL1JPT00vOTQ2NzJkYzAtODc0Mi0xMWU2LWE1NGUtYjU5YTUxM2E5Njhj");
        if (args.length > 0) {
            message.setText(args[0]);
        } else {
            message.setText("Default message from the Java Program: Hello World!");
        }
        Message res = spark.messages().post(message);
        System.out.println("Message posted, result: " + res.getText());

        spark.messages()
            .queryParam("roomId", "Y2lzY29zcGFyazovL3VzL1JPT00vOTQ2NzJkYzAtODc0Mi0xMWU2LWE1NGUtYjU5YTUxM2E5Njhj")
            .queryParam("max", "100")
            .iterate()
            .forEachRemaining(msg -> {
                System.out.println("Message" + msg.getId() + ": " + msg.getText());
             });
        System.out.println("Finishing...");
    }
}

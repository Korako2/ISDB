const POPUP_NOTIFICATION_TIMEOUT = 3000;

const onNewMessage = (msg) => {
    $('#notificationMessage').text(msg);
    $('#notificationModal').modal('show');

    // Automatically close the modal after 3 seconds
    setTimeout(function () {
        $('#notificationModal').modal('hide');
        location.reload();
    }, POPUP_NOTIFICATION_TIMEOUT);
}

$(document).ready(function () {
    const stompClient = new StompJs.Client({
        brokerURL: 'ws://localhost:8080/ws',
    });

    // if url starts with /customer/ then subscribe to /topic/customer
    const topicName = window.location.pathname.startsWith('/customer/') ? '/topic/customer' : '/topic/manager';

    console.log('Subscribing to ' + topicName)

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe(topicName, (message) => {
            onNewMessage(message.body);
        });
    }

    stompClient.onWebSocketError = (error) => {
        console.log('error with websocket: ' + error);
    }

    stompClient.onStompError = (error) => {
        console.log('error: ' + error);
    }

    stompClient.activate();
});

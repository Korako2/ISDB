const POPUP_NOTIFICATION_TIMEOUT = 3000;

const onOrderApproval = (approvalMessage) => {
    $('#approvalMessage').text(approvalMessage);
    $('#approvalModal').modal('show');

    // Automatically close the modal after 3 seconds
    setTimeout(function () {
        $('#approvalModal').modal('hide');
        location.reload();
    }, POPUP_NOTIFICATION_TIMEOUT);
}

$(document).ready(function () {
    const stompClient = new StompJs.Client({
        brokerURL: 'ws://localhost:8080/ws',
    });

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/customer', (message) => {
            onOrderApproval(message.body);
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

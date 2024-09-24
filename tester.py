import socket
import threading

# Server address and port
SERVER_ADDRESS = 'localhost'
SERVER_PORT = 3000
# create a message template with the client's index
MESSAGE = "Hello from client {}"
NUM_CONNECTIONS = 1000

def handle_connection(index):
    try:
        # Create a TCP/IP socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        
        # Connect the socket to the server
        server_address = (SERVER_ADDRESS, SERVER_PORT)
        sock.connect(server_address)
        
        try:
            # Send data
            print(f"Connection {index}: Sending message")
            message = MESSAGE.format(index)
            sock.sendall(message.encode())
            
            # Wait for the response
            response = sock.recv(1024)
            print(f"Connection {index}: Received response: {response.decode()}")
        
        finally:
            # Close the socket
            sock.close()
    
    except Exception as e:
        print(f"Connection {index}: An error occurred: {e}")

# Create threads for each connection
threads = []
for i in range(NUM_CONNECTIONS):
    thread = threading.Thread(target=handle_connection, args=(i,))
    threads.append(thread)
    thread.start()

# Wait for all threads to complete
for thread in threads:
    thread.join()

print("All connections completed.")

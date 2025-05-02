import cv2
from pyzbar.pyzbar import decode

# Initialize the video capture object
cap = cv2.VideoCapture(0)

# Initialize the flag variable
url_shown = False

# Create a named window for the QR code reader
cv2.namedWindow("QR Code Reader", cv2.WINDOW_NORMAL)

# Continuously read frames from the video stream
while True:
    # Read a frame from the video stream
    ret, frame = cap.read()

    # If the frame was not read successfully, exit the loop
    if not ret:
        break

    # Decode any QR codes in the frame
    decoded_objects = decode(frame)

    # Print the decoded URL (if any)
    for obj in decoded_objects:
        if not url_shown:
            print(obj.data.decode("utf-8"))
            url_shown = True

    # Show the frame with QR codes highlighted
    cv2.imshow("QR Code Reader", frame)

    # Exit the loop if the "q" key is pressed or if a QR code is detected
    key = cv2.waitKey(1) & 0xFF
    if key == ord('q') or url_shown:
        break

# Release the video capture object and close the window
cap.release()
cv2.destroyAllWindows()
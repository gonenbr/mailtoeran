This app demonstrates parsing of XTR2 file. This file is binary file containing packets of data in a defined protocol.
I have implemented a Data Repository which is responsible for reading the data and then parse it into array of 16 channels. each channel holds a lot of readings.
The MainActivity observers a live data form the repository, which signals that the data was loaded.  When this happens, the text in the UI changes to let the user know.

Some thoughts: When I will implement the graph based on this data, I will probably change the data into queues.  This will also allow me to start using the collected data, before it is fully loaded.
<img width="380" alt="Screenshot 2024-03-12 at 13 05 46" src="https://github.com/gonenbr/mailtoeran/assets/1392219/10c7cfaf-dac0-4ef7-8c9d-4c3abe99a5dd">


Note: A screenshot is attached to show the data that was collected (using debugger + breakpoint).
<img width="1557" alt="Screenshot 2024-03-10 at 23 02 33" src="https://github.com/gonenbr/mailtoeran/assets/1392219/9f3b83e2-b108-4c17-96fc-913f26b84554">

This app demonstrates parsing of XTR2 file. This file is binary file containing packets of data in a defined protocol.
I have implemented a Data Repository which is responsible for reading the data and then parse it into array of 16 channels. each channel holds a lot of readings.
There's a viewmodel that manage the data through the repository, and orchestrates the timer that sync all the graphs. it provides the accurate data that is needed for each second.

The MainActivity starts the live data timer, that updates the current data for all the graphs, using a live data.
The adapter that holds all the views, for each channel, observes the live data from the viewmodel, and updates the view whenever there's a change in the data.
As a result, every second (or other chosen time), the data for all the graphs is being updates, which causes the UI to be updated.

<img width="380" alt="Screenshot 2024-03-12 at 13 05 46" src="https://github.com/gonenbr/mailtoeran/assets/1392219/10c7cfaf-dac0-4ef7-8c9d-4c3abe99a5dd">


Note: A screenshot is attached to show the data that was collected (using debugger + breakpoint).
<img width="1557" alt="Screenshot 2024-03-10 at 23 02 33" src="https://github.com/gonenbr/mailtoeran/assets/1392219/9f3b83e2-b108-4c17-96fc-913f26b84554">

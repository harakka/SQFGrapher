# SQFGrapher
A project from 2012-2013, uploaded for historical curiosity. Generates a call graph from Arma 2 SQF script files and outputs it as an edge list csv file. If the code breaks, you get to keep both parts.

Run with with `java org.myrskynkantaja.harakka.sqfcallgraph.SQFCallGraph path\to\folder\full\of\sqf location\of\output\csv\file`

This program was developed to help with documentation of [the F2 framework](https://github.com/ferstaberinde/F2/) back in the Arma 2 days. I recall there was some attempt to update it to Arma 3 SQF changes when we started to work on F2's successor, [F3](https://github.com/ferstaberinde/F3/), but that work was never finished.

The program parses through a folder of sqf code, starting from init.sqf, and descends into subfolders as needed. It follows all the different commands that execute code from other files (`execvm`, `#include`, `addaction`, `compile preprocessfile` and `compile preprocessfilelinenumbers`) and stores the resulting connections between files as an edge graph as rows in a CSV file, in the format
```file_from_which_call_was_initiated,file_which_was_called,command_type```
The command type can thus be used as a label for the edge between nodes.

For visualization the process I used was:
1. Import the resulting CSV file into Google Sheets
2. Export from Google Sheets as XLSX
3. Import XSLX into [yEd](https://yed.yworks.com/support/manual/import_excel.html), with Edge Representation: Edge List, Data Range: column A to column C, Source IDs from column A, Target IDs from column B, and Label Text: Node Label. Then run the resulting graph through Hierarchic Layout tool and set the various label font settings and such in whichever way you find readable.
4. Export resulting graph into png from yEd.

An example of such a graph, generated from latest release of F2 at the time (2.7.3), is included below.

![Example graph](https://github.com/harakka/SQFGrapher/raw/main/example.png "Logo Title Text 1")

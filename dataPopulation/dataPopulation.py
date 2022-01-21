from utilities.commandPrompt import CommandPrompt
import sys

cp = CommandPrompt()

"""
if it is specified one or more command parameter, tries to interprete the command
else opens the command prompt
"""

if __name__ == "__main__":
    command_parameters_list = sys.argv[1:]
    if len(command_parameters_list) > 0:
        cp.run_command(command_parameters_list)
    else:
        try:
            cp.start()
        except KeyboardInterrupt:
            cp.stop()
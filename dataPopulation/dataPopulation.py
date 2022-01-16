from utilities.commandPrompt import CommandPrompt


cp = CommandPrompt()
try:
    cp.start()
except KeyboardInterrupt:
    cp.stop()
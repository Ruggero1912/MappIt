import logging

class ClientUtilities:
    def get_logger(logger_name : str):
        logger = logging.getLogger(logger_name)
        logger.setLevel(level=logging.DEBUG)
        ch = logging.StreamHandler()
        ch.setLevel(level=logging.DEBUG)
        formatter = logging.Formatter('%(asctime)s - %(name)s - [%(levelname)s] - %(message)s')
        ch.setFormatter(formatter)
        logger.addHandler(ch)
        return logger
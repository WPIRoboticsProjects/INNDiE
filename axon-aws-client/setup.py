from setuptools import setup, find_packages

setup(
    name="axonawsclient",
    version="0.1.0",
    packages=find_packages(),
    entry_points="""
        [console_scripts]
        axonawsclient=axonawsclient:cli
    """
)

from setuptools import setup

setup(
    name="client",
    version="0.1.0",
    py_modules=["client"],
    install_requires=[
        "click", "boto3", 'ipify'
    ],
    entry_points="""
        [console_scripts]
        client=client:cli
    """
)

from setuptools import setup, find_packages
from os import path

here = path.abspath(path.dirname(__file__))

# Get the long description from the README file
with open(path.join(here, 'README.md'), encoding='utf-8') as f:
    long_description = f.read()

setup(
    name="axon",
    version="0.1.0",
    description="The Axon CLI",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/wpilibsuite/Axon",
    packages=["axon"],
    python_requires=">=3.6",
    install_requires=["click", "boto3", "ipify"],
    entry_points={
        "console_scripts": ["axon=axon.client:cli"]
    },
    project_urls={
        "Bug Reports": "https://github.com/wpilibsuite/Axon/issues",
        "Source": "https://github.com/wpilibsuite/Axon"
    }
)

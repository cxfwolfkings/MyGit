# jupyter

jupyter有所见即所得的效果，支持非常多的编程语言，而且可以直接运行这些语言的代码。

要让 Jupyter Notebook 能够运行特定语言的代码，需要添加对应的内核。具体支持的语言及内核可以查看该链接：[https://github.com/jupyter/jupyter/wiki/Jupyter-kernels](https://github.com/jupyter/jupyter/wiki/Jupyter-kernels)

```sh
# 安装
pip install jupyterlab

# 检查Jupyter是否正确安装
jupyter kernelspec list
#-----------------------------------------------------------------
#Available kernels:
#  python3  D:\ProgramData\Anaconda3\share\jupyter\kernels\python3
#-----------------------------------------------------------------
# 启动
jupyter notebook
```

**.NET环境**

```sh
# 安装.NET Interactive
dotnet tool install --global Microsoft.dotnet-interactive
# 查看已经安装的全局工具
dotnet tool list -g
# 安装 jupyter kernal
dotnet interactive jupyter install
# 再次检查安装好的.NET版本Jupyter
jupyter kernelspec list
```

**Java环境**

```sh
cd "D:\Arms\ijava-1.3.0"
python install.py --sys-prefix

# 第二种方法
# Install scijava-jupyter-kernel with :
# Add the conda-forge channel
conda config --add channels conda-forge
# Create an isolated environment called `java_env` and install the kernel（安装失败）
conda create --name java_env scijava-jupyter-kernel
# Activate the `java_env` environment
conda activate java_env
# Check the kernel has been installed
jupyter kernelspec list
# Launch your favorite Jupyter client
jupyter notebook
# or
jupyter lab
# Note : It is strongly suggested to install the kernel in an isolated Conda environment (not in the root environment).
```


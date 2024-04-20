linux上安装minio
```shell
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio
MINIO_ROOT_USER=登录名 MINIO_ROOT_PASSWORD=密码 nohup /data/minio/minio server /data/minioData --console-address ":9001" > /data/log/minio.log 2>&1 &#
```

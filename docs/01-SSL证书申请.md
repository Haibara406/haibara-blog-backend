# SSL证书申请指南

## 前置要求
- DNS已指向 154.94.235.178
- Nginx容器运行中且80端口可访问
- `/data/nginx/html` 目录存在

## 1. 安装Certbot
```bash
apt update && apt install -y certbot
```

## 2. 申请证书
```bash
# 申请所有域名的证书
certbot certonly --webroot \
  -w /data/nginx/html \
  -d haikari.top \
  -d www.haikari.top \
  -d sherry.haikari.top \
  -d blog.admin.haikari.top \
  -d minio.haikari.top \
  --email Haibara406@gmail.com \
  --agree-tos \
  --non-interactive
```

# 临时nginx配置：
cat > /data/nginx/conf/conf.d/http-only.conf << 'EOF'
server {
listen 80;
server_name haikari.top www.haikari.top sherry.haikari.top blog.admin.haikari.top minio.haikari.top;

    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
        try_files $uri =404;
    }
    
    location / {
        return 200 "Waiting for SSL certificate...";
        add_header Content-Type text/plain;
    }
}
EOF

## 3. 复制证书到Nginx目录
```bash
# 创建ssl目录
mkdir -p /data/nginx/ssl

# 复制haikari.top证书
cp /etc/letsencrypt/live/haikari.top/fullchain.pem /data/nginx/ssl/haikari.top.crt
cp /etc/letsencrypt/live/haikari.top/privkey.pem /data/nginx/ssl/haikari.top.key

# 复制sherry.haikari.top证书
ln -s /data/nginx/ssl/haikari.top.crt /data/nginx/ssl/sherry.haikari.top.crt
ln -s /data/nginx/ssl/haikari.top.key /data/nginx/ssl/sherry.haikari.top.key

# 复制blog.admin.haikari.top证书
ln -s /data/nginx/ssl/haikari.top.crt /data/nginx/ssl/blog.admin.haikari.top.crt
ln -s /data/nginx/ssl/haikari.top.key /data/nginx/ssl/blog.admin.haikari.top.key

# 复制minio.haikari.top证书
ln -s /data/nginx/ssl/haikari.top.crt /data/nginx/ssl/minio.haikari.top.crt
ln -s /data/nginx/ssl/haikari.top.key /data/nginx/ssl/minio.haikari.top.key

# 设置权限
chmod 644 /data/nginx/ssl/*.crt
chmod 600 /data/nginx/ssl/*.key
```

## 4. 重启Nginx
```bash
docker restart nginx
```

## 5. 设置自动续期（证书90天有效）
```bash
# 创建续期脚本
cat > /root/renew-ssl.sh << 'EOF'
#!/bin/bash
certbot renew --quiet

# 只需要复制一套证书
cp /etc/letsencrypt/live/haikari.top/fullchain.pem /data/nginx/ssl/haikari.top.crt
cp /etc/letsencrypt/live/haikari.top/privkey.pem /data/nginx/ssl/haikari.top.key

# 如果使用方案二，再复制其他副本
cp /etc/letsencrypt/live/haikari.top/fullchain.pem /data/nginx/ssl/sherry.haikari.top.crt
cp /etc/letsencrypt/live/haikari.top/privkey.pem /data/nginx/ssl/sherry.haikari.top.key
cp /etc/letsencrypt/live/haikari.top/fullchain.pem /data/nginx/ssl/blog.admin.haikari.top.crt
cp /etc/letsencrypt/live/haikari.top/privkey.pem /data/nginx/ssl/blog.admin.haikari.top.key
cp /etc/letsencrypt/live/haikari.top/fullchain.pem /data/nginx/ssl/minio.haikari.top.crt
cp /etc/letsencrypt/live/haikari.top/privkey.pem /data/nginx/ssl/minio.haikari.top.key

# 重启nginx
docker restart nginx
EOF

# 赋予执行权限
chmod +x /root/renew-ssl.sh

# 添加定时任务（每月1号凌晨执行）
(crontab -l 2>/dev/null; echo "0 0 1 * * /root/renew-ssl.sh >> /var/log/ssl-renew.log 2>&1") | crontab -
```

## 验证
```bash
# 检查证书文件
ls -lh /data/nginx/ssl/

# 检查证书有效期
openssl x509 -in /data/nginx/ssl/haikari.top.crt -noout -dates
```


# Nginx反向代理配置

## 创建配置文件
```bash
# 创建配置目录
mkdir -p /data/nginx/conf/conf.d

# 创建配置文件
vim /data/nginx/conf/conf.d/blog.conf
```

## 配置内容
将以下内容复制到 `/data/nginx/conf/conf.d/blog.conf`：

```nginx
# ==================== HTTP重定向到HTTPS ====================
server {
    listen 80;
    server_name haikari.top www.haikari.top;
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
    }
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 80;
    server_name sherry.haikari.top;
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
    }
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 80;
    server_name blog.admin.haikari.top;
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
    }
    location / {
        return 301 https://$server_name$request_uri;
    }
}

server {
    listen 80;
    server_name minio.haikari.top;
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html;
        try_files $uri =404;
    }
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# ==================== 主域名 HTTPS ====================
server {
    listen 443 ssl;
    http2 on;
    server_name haikari.top www.haikari.top;

    ssl_certificate /etc/nginx/ssl/haikari.top.crt;
    ssl_certificate_key /etc/nginx/ssl/haikari.top.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html =404;
    }

    # MinIO文件服务代理
    location /files/ {
        proxy_pass http://154.94.235.178:9000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # MinIO存储桶代理
    location /haibara-blog/ {
        proxy_pass http://154.94.235.178:9000/haibara-blog/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://172.17.0.1:8062/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端直接路由代理
    location ~ ^/(websiteInfo|user|banners|tag|article|comment|link|category|menu|leaveWord|favorite|treeHole|role|permission|log|loginLog|blackList|oauth|public|like|photo|monitor)(/.*)?$ {
        proxy_pass http://172.17.0.1:8062$request_uri;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 其他服务代理
    location /wapi/ {
        proxy_pass http://172.17.0.1:3000/;
    }
}

# ==================== 前台博客 HTTPS ====================
server {
    listen 443 ssl;
    http2 on;
    server_name sherry.haikari.top;

    ssl_certificate /etc/nginx/ssl/sherry.haikari.top.crt;
    ssl_certificate_key /etc/nginx/ssl/sherry.haikari.top.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html/blog;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # MinIO存储桶代理
    location /haibara-blog/ {
        proxy_pass http://154.94.235.178:9000/haibara-blog/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://172.17.0.1:8062/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /wapi/ {
        proxy_pass http://172.17.0.1:3000/;
    }
}

# ==================== 管理端 HTTPS ====================
server {
    listen 443 ssl;
    http2 on;
    server_name blog.admin.haikari.top;

    ssl_certificate /etc/nginx/ssl/blog.admin.haikari.top.crt;
    ssl_certificate_key /etc/nginx/ssl/blog.admin.haikari.top.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html/admin;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # MinIO存储桶代理
    location /haibara-blog/ {
        proxy_pass http://154.94.235.178:9000/haibara-blog/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 导出功能优化配置
    location /api/export/ {
        proxy_pass http://172.17.0.1:8062/export/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 120s;
        proxy_send_timeout 120s;
        proxy_buffering off;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://172.17.0.1:8062/;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# ==================== MinIO HTTPS ====================
server {
    listen 443 ssl;
    http2 on;
    server_name minio.haikari.top;

    ssl_certificate /etc/nginx/ssl/minio.haikari.top.crt;
    ssl_certificate_key /etc/nginx/ssl/minio.haikari.top.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # WebSocket连接支持
    location /ws/ {
        proxy_pass http://154.94.235.178:9001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 86400;
    }

    # MinIO Web管理界面
    location / {
        proxy_pass http://154.94.235.178:9001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        proxy_buffering off;
        proxy_request_buffering off;
    }

    # MinIO API和文件访问
    location /haibara-blog/ {
        proxy_pass http://154.94.235.178:9000/haibara-blog/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 重新加载Nginx
```bash
# 测试配置文件语法
docker exec nginx nginx -t

# 重新加载配置
docker exec nginx nginx -s reload

# 或者重启容器
docker restart nginx
```

## 验证配置
```bash
# 查看nginx日志
docker logs nginx

# 测试HTTP到HTTPS重定向
curl -I http://haikari.top

# 测试HTTPS访问
curl -I https://haikari.top
```


FROM nginx:alpine
# Copy nginx.conf as a template for envsubst to process environment variables at startup
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY . /usr/share/nginx/html
EXPOSE 80

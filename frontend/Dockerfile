FROM node:20-alpine AS build
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
RUN npm run build
FROM nginx:1.27-alpine
ENV PORT=80
ENV BACKEND_HOSTPORT=backend:8080
ENV NGINX_ENVSUBST_FILTER=^(PORT|BACKEND_HOSTPORT)$
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/templates/default.conf.template
EXPOSE 80

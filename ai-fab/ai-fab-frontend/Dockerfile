# syntax=docker/dockerfile:1
# Dockerfile for AI Fabric Vue Frontend (ai-fab-frontend)

# Build stage
FROM node:18-alpine AS builder

WORKDIR /app

COPY ai-fab/ai-fab-frontend/package*.json ./

RUN npm ci

COPY ai-fab/ai-fab-frontend/ ./

ARG VITE_API_BASE_URL=http://localhost:8080
ARG VITE_PYTHON_API_URL=http://localhost:8000

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_PYTHON_API_URL=$VITE_PYTHON_API_URL

RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY ai-fab/ai-fab-frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

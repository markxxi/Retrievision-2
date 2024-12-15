# -*- coding: utf-8 -*-
"""dimen.ipynb

Automatically generated by Colab.

Original file is located at
    https://colab.research.google.com/drive/1uoaJvbsGnOAQYjo1-5juurESqhLy0fPv
"""

!pip install ultralytics
!pip install torch==1.8.0
!pip install torchvision==0.12.0
!pip install meson

import torch
import torchvision
import numpy as np
import cv2
from ultralytics import YOLO
import json
from io import BytesIO

class ObjectDetectionWithLogo:
    def __init__(self, model_path, logo_path):
        self.model = YOLO(model_path)  # YOLOv8 model
        self.logo_path = logo_path     # Logo file path
        self.logo = cv2.imread(self.logo_path, cv2.IMREAD_UNCHANGED)

    def bitmap_to_numpy(self, bitmap_data):
        """Convert byte array to a numpy array (OpenCV format)."""
        img_array = np.frombuffer(bitmap_data, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)  # Decode the image
        return img

    def detect_objects(self, image_with_logo, logo_real_width=5.8, logo_real_height=5.8):
        results = self.model(image_with_logo)
        detected_objects = []

        logo_pixel_width = self.logo.shape[1]
        logo_pixel_height = self.logo.shape[0]

        pixel_per_metric_width = logo_pixel_width / logo_real_width
        pixel_per_metric_height = logo_pixel_height / logo_real_height

        for r in results:
            boxes = r.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0]
                width_in_pixels = (x2 - x1).item()
                height_in_pixels = (y2 - y1).item()

                real_world_width = width_in_pixels / pixel_per_metric_width
                real_world_height = height_in_pixels / pixel_per_metric_height

                class_id = int(box.cls[0].item())
                class_name = self.model.names[class_id]

                detected_objects.append({
                    'class_name': class_name,
                    'width_in_inches': real_world_width,
                    'height_in_inches': real_world_height,
                    'bounding_box': (x1.item(), y1.item(), x2.item(), y2.item())
                })

        return detected_objects

    def process_image_from_bytes(self, bitmap_data):
        """Process the image from byte data, perform object detection and return results."""
        image = self.bitmap_to_numpy(bitmap_data)  # Convert the byte data to an image
        detected_objects = self.detect_objects(image)
        results_json = json.dumps(detected_objects, indent=4)
        return results_json

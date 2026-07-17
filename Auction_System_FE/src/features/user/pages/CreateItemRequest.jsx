import React, { useState, useEffect } from 'react';
import { Form, Input, InputNumber, Select, Upload, Button, message, Card, Typography, Row, Col, Space } from 'antd';
import { InboxOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { itemApi } from '../../../api/itemApi';
import { categoryApi } from '../../../api/categoryApi';

const { Title, Text } = Typography;
const { Dragger } = Upload;
const { Option } = Select;

const CreateItemRequest = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState([]);
  const [fileList, setFileList] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryApi.getAll();
        setCategories(response || []);
      } catch (error) {
        message.error('Failed to load categories');
      }
    };
    fetchCategories();
  }, []);

  const handleUploadChange = ({ fileList: newFileList }) => {
    setFileList(newFileList);
  };

  const beforeUpload = (file) => {
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      message.error(`${file.name} is too large. Image must be smaller than 5MB!`);
      return Upload.LIST_IGNORE;
    }
    return false; // Prevent automatic upload
  };

  const onFinish = async (values) => {
    if (fileList.length === 0) {
      message.error('Please upload at least 1 image for the item.');
      return;
    }

    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('itemName', values.itemName);
      formData.append('categoryId', values.categoryId);
      formData.append('reservePrice', values.reservePrice);
      formData.append('minIncrement', values.minIncrement);
      formData.append('description', values.description);
      formData.append('condition', values.condition);

      fileList.forEach((file) => {
        formData.append('images', file.originFileObj || file);
      });

      await itemApi.createWithFormData(formData);
      message.success('Item request submitted successfully!');
      navigate('/my-account'); // Redirect to My Account (Auction Items tab)
    } catch (error) {
      message.error(error.message || 'Failed to submit item request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <Button 
        type="link" 
        icon={<ArrowLeftOutlined />} 
        onClick={() => navigate('/my-account')}
        style={{ marginBottom: '16px', paddingLeft: 0 }}
      >
        Back to My Items
      </Button>
      
      <Card>
        <Title level={2} style={{ textAlign: 'center', marginBottom: '24px' }}>Register Item for Auction</Title>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ condition: 'NEW' }}
        >
          <Row gutter={16}>
            <Col xs={24}>
              <Form.Item
                name="itemName"
                label="Item Name"
                rules={[
                  { required: true, message: 'Please enter item name' },
                  { min: 10, max: 150, message: 'Name must be between 10 and 150 characters' }
                ]}
              >
                <Input placeholder="Enter item name" />
              </Form.Item>
            </Col>
            
            <Col xs={24} md={12}>
              <Form.Item
                name="categoryId"
                label="Category"
                rules={[{ required: true, message: 'Please select a category' }]}
              >
                <Select placeholder="Select category">
                  {categories.map((cat) => (
                    <Option key={cat.id || cat.categoryId} value={cat.id || cat.categoryId}>
                      {cat.name || cat.categoryName}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={12}>
              <Form.Item
                name="condition"
                label="Condition"
                rules={[{ required: true, message: 'Please select item condition' }]}
              >
                <Select placeholder="Select condition">
                  <Option value="NEW">New</Option>
                  <Option value="LIKE_NEW">Like New</Option>
                  <Option value="GOOD">Good</Option>
                  <Option value="FAIR">Fair</Option>
                  <Option value="POOR">Poor</Option>
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={12}>
              <Form.Item
                name="reservePrice"
                label="Reserve Price (VNĐ)"
                dependencies={['minIncrement']}
                rules={[
                  { required: true, message: 'Please enter reserve price' },
                  { type: 'number', min: 1000, message: 'Price must be at least 1,000 VNĐ' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      const minIncr = getFieldValue('minIncrement');
                      if (!value || !minIncr || value >= minIncr) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('Reserve price must be greater than or equal to minimum increment!'));
                    },
                  }),
                ]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value?.replace(/\$\s?|(,*)/g, '')}
                  placeholder="Enter reserve price"
                  addonAfter="VNĐ"
                  min={1000}
                  step={10000}
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={12}>
              <Form.Item
                name="minIncrement"
                label="Min Increment (VNĐ)"
                dependencies={['reservePrice']}
                rules={[
                  { required: true, message: 'Please enter minimum increment' },
                  { type: 'number', min: 1000, message: 'Increment must be at least 1,000 VNĐ' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      const reserve = getFieldValue('reservePrice');
                      if (!value || !reserve || value <= reserve) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('Minimum increment cannot exceed the reserve price!'));
                    },
                  }),
                ]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value?.replace(/\$\s?|(,*)/g, '')}
                  placeholder="Enter min increment"
                  addonAfter="VNĐ"
                  min={1000}
                  step={10000}
                />
              </Form.Item>
            </Col>

            <Col xs={24}>
              <Form.Item
                name="description"
                label="Description"
                rules={[{ required: true, message: 'Please enter item description' }]}
              >
                <Input.TextArea rows={4} placeholder="Describe the item details, condition, and history" />
              </Form.Item>
            </Col>

            <Col xs={24}>
              <Form.Item label="Images" required>
                <Dragger
                  name="file"
                  multiple
                  fileList={fileList}
                  beforeUpload={beforeUpload}
                  onChange={handleUploadChange}
                  maxCount={5}
                  listType="picture"
                  accept="image/*,video/*"
                >
                  <p className="ant-upload-drag-icon">
                    <InboxOutlined />
                  </p>
                  <p className="ant-upload-text">Click or drag file to this area to upload</p>
                  <p className="ant-upload-hint">
                    Support for a single or bulk upload. Maximum 5 files, 5MB each.
                  </p>
                </Dragger>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ marginTop: '24px', textAlign: 'right' }}>
            <Space>
              <Button onClick={() => navigate('/my-account')}>Cancel</Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                Submit Request
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default CreateItemRequest;

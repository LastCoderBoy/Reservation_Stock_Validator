import { useState } from 'react';
import { Button, Input, Card, CardHeader, CardBody, CardFooter, Badge, Modal, ConfirmModal, Spinner, PageSpinner } from '../components/common';

export const ComponentShowcasePage = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [showPageSpinner, setShowPageSpinner] = useState(false);

  if (showPageSpinner) {
    return <PageSpinner label="Loading showcase..." />;
  }

  return (
    <div className="min-h-screen bg-primary-50 py-12 px-6">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-4xl font-bold text-text-primary mb-2">Component Showcase</h1>
        <p className="text-text-secondary mb-12">
          Preview of all reusable UI components with Minimal Luxury theme
        </p>

        {/* Buttons */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Buttons</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="space-y-6">
              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Variants</h3>
                <div className="flex flex-wrap gap-3">
                  <Button variant="primary">Primary</Button>
                  <Button variant="secondary">Secondary</Button>
                  <Button variant="danger">Danger</Button>
                  <Button variant="outline">Outline</Button>
                  <Button variant="ghost">Ghost</Button>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Sizes</h3>
                <div className="flex flex-wrap items-center gap-3">
                  <Button size="sm">Small</Button>
                  <Button size="md">Medium</Button>
                  <Button size="lg">Large</Button>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">States</h3>
                <div className="flex flex-wrap gap-3">
                  <Button isLoading>Loading</Button>
                  <Button disabled>Disabled</Button>
                  <Button leftIcon="🚀">With Left Icon</Button>
                  <Button rightIcon="→">With Right Icon</Button>
                  <Button fullWidth>Full Width</Button>
                </div>
              </div>
            </div>
          </Card>
        </section>

        {/* Inputs */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Inputs</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Input label="Default Input" placeholder="Enter text..." />
              <Input label="With Helper Text" placeholder="Enter email..." helperText="We'll never share your email." />
              <Input label="With Error" placeholder="Enter password..." error="Password is required" />
              <Input 
                label="With Icons" 
                placeholder="Search..." 
                leftIcon={<span>🔍</span>}
                rightIcon={<span>✓</span>}
              />
              <Input label="Disabled Input" placeholder="Can't edit" disabled value="Disabled value" />
              <Input type="password" label="Password" placeholder="Enter password..." />
            </div>
          </Card>
        </section>

        {/* Cards */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Cards</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card variant="default">
              <CardHeader title="Default Card" subtitle="Border style" />
              <CardBody>
                <p className="text-text-secondary">This is a default card with border styling.</p>
              </CardBody>
            </Card>

            <Card variant="elevated">
              <CardHeader title="Elevated Card" subtitle="Shadow style" />
              <CardBody>
                <p className="text-text-secondary">This card has a shadow for depth.</p>
              </CardBody>
            </Card>

            <Card variant="bordered" hover>
              <CardHeader title="Hoverable Card" subtitle="Interactive" />
              <CardBody>
                <p className="text-text-secondary">Hover over me to see the effect!</p>
              </CardBody>
              <CardFooter>
                <Button size="sm" fullWidth>Action</Button>
              </CardFooter>
            </Card>
          </div>
        </section>

        {/* Badges */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Badges</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="space-y-6">
              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Variants</h3>
                <div className="flex flex-wrap gap-3">
                  <Badge variant="success">Success</Badge>
                  <Badge variant="warning">Warning</Badge>
                  <Badge variant="danger">Danger</Badge>
                  <Badge variant="info">Info</Badge>
                  <Badge variant="neutral">Neutral</Badge>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Sizes</h3>
                <div className="flex flex-wrap items-center gap-3">
                  <Badge size="sm">Small</Badge>
                  <Badge size="md">Medium</Badge>
                  <Badge size="lg">Large</Badge>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">With Icons & Dots</h3>
                <div className="flex flex-wrap gap-3">
                  <Badge variant="success" icon="✓">Confirmed</Badge>
                  <Badge variant="warning" icon="⚠️">Pending</Badge>
                  <Badge variant="danger" icon="✕">Cancelled</Badge>
                  <Badge variant="success" dot>Active</Badge>
                  <Badge variant="neutral" dot>Inactive</Badge>
                </div>
              </div>
            </div>
          </Card>
        </section>

        {/* Spinners */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Spinners</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="space-y-6">
              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Sizes</h3>
                <div className="flex flex-wrap items-center gap-8">
                  <Spinner size="sm" />
                  <Spinner size="md" />
                  <Spinner size="lg" />
                  <Spinner size="xl" />
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">With Labels</h3>
                <div className="flex flex-wrap gap-8">
                  <Spinner label="Loading..." />
                  <Spinner color="primary" label="Processing..." />
                </div>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-text-secondary mb-3">Page Spinner Demo</h3>
                <Button onClick={() => {
                  setShowPageSpinner(true);
                  setTimeout(() => setShowPageSpinner(false), 2000);
                }}>
                  Show Page Spinner (2s)
                </Button>
              </div>
            </div>
          </Card>
        </section>

        {/* Modals */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Modals</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="flex flex-wrap gap-3">
              <Button onClick={() => setIsModalOpen(true)}>Open Modal</Button>
              <Button variant="danger" onClick={() => setIsConfirmModalOpen(true)}>
                Open Confirm Modal
              </Button>
            </div>
          </Card>

          {/* Sample Modal */}
          <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title="Sample Modal"
            footer={
              <>
                <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={() => setIsModalOpen(false)}>Confirm</Button>
              </>
            }
          >
            <p className="text-text-primary mb-4">
              This is a sample modal with custom content. You can put any React components here.
            </p>
            <Input label="Sample Input" placeholder="Enter something..." />
          </Modal>

          {/* Confirm Modal */}
          <ConfirmModal
            isOpen={isConfirmModalOpen}
            onClose={() => setIsConfirmModalOpen(false)}
            onConfirm={() => {
              alert('Confirmed!');
              setIsConfirmModalOpen(false);
            }}
            title="Confirm Action"
            message="Are you sure you want to proceed with this action? This cannot be undone."
            variant="danger"
            confirmText="Yes, Delete"
            cancelText="No, Cancel"
          />
        </section>

        {/* Color Palette */}
        <section className="mb-12">
          <h2 className="text-2xl font-bold text-text-primary mb-6">Color Palette (Minimal Luxury)</h2>
          
          <Card variant="elevated" className="p-6">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
              <div>
                <div className="h-20 bg-primary-50 rounded-lg border border-secondary-200 mb-2"></div>
                <p className="text-xs font-semibold">Primary 50</p>
                <p className="text-xs text-text-secondary">#FEFEFE</p>
              </div>
              <div>
                <div className="h-20 bg-secondary-100 rounded-lg mb-2"></div>
                <p className="text-xs font-semibold">Secondary 100</p>
                <p className="text-xs text-text-secondary">#E8E4DC</p>
              </div>
              <div>
                <div className="h-20 bg-accent-600 rounded-lg mb-2"></div>
                <p className="text-xs font-semibold">Accent 600</p>
                <p className="text-xs text-text-secondary">#B8860B</p>
              </div>
              <div>
                <div className="h-20 bg-urgency-warning rounded-lg mb-2"></div>
                <p className="text-xs font-semibold">Warning</p>
                <p className="text-xs text-text-secondary">#D97706</p>
              </div>
            </div>
          </Card>
        </section>
      </div>
    </div>
  );
};
